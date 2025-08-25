package no.nav.permitteringsskjemaapi.altinn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.benmanes.caffeine.cache.Caffeine
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
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException

@Component
class AltinnService(
    restTemplateBuilder: RestTemplateBuilder,
    private val authenticatedUserHolder: AuthenticatedUserHolder,
    private val tokenExchangeClient: TokenExchangeClient,

    @Value("\${nais.cluster.name}") private val naisCluster: String,
) {

    val cache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .recordStats()
        .build<String, AltinnTilgangerResponse>()

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

    fun hentAlleOrgnr(tilgang: String) = hentAltinnTilganger(tilgang).tilgangTilOrgNr[tilgang] ?: emptySet()

    fun harTilgang(orgnr: String, tilgang: String) =
        hentAltinnTilganger(tilgang).orgNrTilTilganger[orgnr]?.contains(tilgang) ?: false

    fun hentAltinnTilganger(tilgang: String) = cache.getIfPresent(authenticatedUserHolder.token) ?: run {
        hentAltinnTilgangerFraProxy(tilgang).also {
            cache.put(authenticatedUserHolder.token, it)
        }
    }

    private fun hentAltinnTilgangerFraProxy(tilgang: String): AltinnTilgangerResponse {
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
                .body(
                    AltinnTilgangerRequest(
                        filter = AltinnTilgangerFilter(
                            altinn3Tilganger = listOf(tilgang)
                        )
                    )
                ),
            AltinnTilgangerResponse::class.java
        )

        return response.body!! // response != 200 => throws
    }
}

data class AltinnTilgangerRequest(
    val filter: AltinnTilgangerFilter
)

data class AltinnTilgangerFilter(
    val altinn3Tilganger: List<String>
)

data class AltinnTilgang(
    val orgnr: String,
    val navn: String,
    val organisasjonsform: String,
    val underenheter: List<AltinnTilgang>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnTilgangerResponse(
    val isError: Boolean,
    val hierarki: List<AltinnTilgang>,
    val orgNrTilTilganger: Map<String, Set<String>>,
    val tilgangTilOrgNr: Map<String, Set<String>>,
)
