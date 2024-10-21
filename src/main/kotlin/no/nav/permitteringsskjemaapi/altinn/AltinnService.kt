package no.nav.permitteringsskjemaapi.altinn

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

@Component
class AltinnService(
    restTemplateBuilder: RestTemplateBuilder,
    private val authenticatedUserHolder: AuthenticatedUserHolder,
    private val tokenExchangeClient: TokenExchangeClient,

    @Value("\${nais.cluster.name}") private val naisCluster: String,
) {

    internal val restTemplate = restTemplateBuilder
        .additionalInterceptors(
            retryInterceptor(
                maxAttempts = 3,
                backoffPeriod = 250L,
                SocketException::class.java,
                SSLHandshakeException::class.java,
                ResourceAccessException::class.java,
            )
        )
        .build()

    fun hentAltinnTilganger() = hentAltinnTilgangerFraProxy()

    fun hentOrganisasjonerBasertPåRettigheter(
        serviceKode: String,
        serviceEdition: String
    ) = hentAltinnTilganger().tilgangTilOrgNr["${serviceKode}:${serviceEdition}"] ?: emptySet()

    private fun hentAltinnTilgangerFraProxy(): AltinnTilganger {
        val token = tokenExchangeClient.exchange(
            authenticatedUserHolder.token,
            "$naisCluster:fager:arbeidsgiver-altinn-tilganger"
        )
        val response = restTemplate.exchange(
            RequestEntity
                .method(HttpMethod.POST, "http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .headers {
                    it.setBearerAuth(token.access_token!!)
                }
                .build(),
            AltinnTilganger::class.java
        )

        return response.body!! // response != 200 => throws
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnTilganger(
    val isError: Boolean,
    val hierarki: List<AltinnTilgang>,
    val orgNrTilTilganger: Map<String, Set<String>>,
    val tilgangTilOrgNr: Map<String, Set<String>>,
) {
    data class AltinnTilgang(
        val orgNr: String,
        val underenheter: List<AltinnTilgang>,
        val name: String,
        val organizationForm: String,
    )

    @get:JsonIgnore
    val alleOrgNr : Set<String>
        get() = hierarki.flatMap { flattenUnderOrganisasjoner(it) }.toSet()

    private fun flattenUnderOrganisasjoner(
        altinnTilgang: AltinnTilgang,
        parentOrgNr: String? = null
    ): Set<String> {
        val children = altinnTilgang.underenheter.flatMap { flattenUnderOrganisasjoner(it, altinnTilgang.orgNr) }
        return setOfNotNull(parentOrgNr, altinnTilgang.orgNr) + children
    }
}
