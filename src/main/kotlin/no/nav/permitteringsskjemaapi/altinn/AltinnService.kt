package no.nav.permitteringsskjemaapi.altinn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.tokenx.TokenXToken
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.RequestEntity
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.exchange
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

interface AltinnService {
    fun hentOrganisasjoner(): List<Organisasjon>
    fun hentOrganisasjonerBasertPåRettigheter(serviceKode: String, serviceEdition: String): List<Organisasjon>
}

@Profile("prod-gcp", "test")
@Component
class AltinnServiceImpl(
    private val altinnConfig: AltinnConfig,
    clientConfigurationProperties: ClientConfigurationProperties,
    oAuth2AccessTokenService: OAuth2AccessTokenService,
    restTemplateBuilder: RestTemplateBuilder,
) : AltinnService {

    private val restTemplate = restTemplateBuilder
        .additionalInterceptors(
            ClientHttpRequestInterceptor { request, body, execution ->
                val registration = clientConfigurationProperties.registration["altinn-rettigheter-client"]!!
                val accessTokenResponse = oAuth2AccessTokenService.getAccessToken(registration)
                request.headers.setBearerAuth(accessTokenResponse.accessToken!!)
                request.headers["x-consumer-id"] = "permitteringsskjema-api"
                execution.execute(request, body)
            },
        )
        .build()

    private val log = logger()

    override fun hentOrganisasjoner() = getFromAltinn(
        "${altinnConfig.altinnProxyUrl}reportees/?ForceEIAuthentication&\$filter=Type+ne+'Person'+and+Status+eq+'Active'",
    ).toOrganisasjoner()

    override fun hentOrganisasjonerBasertPåRettigheter(
        serviceKode: String,
        serviceEdition: String
    ) = getFromAltinn(
        "${altinnConfig.altinnProxyUrl}reportees/?ForceEIAuthentication&\$filter=Type+ne+'Person'+and+Status+eq+'Active'&serviceCode=$serviceKode&serviceEdition=$serviceEdition",
    ).toOrganisasjoner()

    fun getFromAltinn(
        url: String,
    ): List<Altinn2Organisasjon> {
        val response: MutableSet<Altinn2Organisasjon> = HashSet()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            val pageQuery = "&\$top=$PAGE_SIZE&\$skip=${pageNumber * PAGE_SIZE}"
            hasMore = try {
                restTemplate.exchange<List<Altinn2Organisasjon>>("$url$pageQuery", HttpMethod.GET, null).body?.let {
                    response.addAll(it)
                    it.size >= PAGE_SIZE
                } ?: false
            } catch (exception: RestClientException) {
                log.error("Feil fra Altinn med spørring: : Exception:" + exception.message)
                throw PermitteringsApiException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt")
            }
            pageNumber += 1
        }
        return response.toList()
    }

    private fun List<Altinn2Organisasjon>.toOrganisasjoner() =
        mapNotNull {
            val organizationNumber = it.organizationNumber
            val organizationForm = it.organizationForm
            if (organizationNumber == null || organizationForm == null)
                null
            else
                Organisasjon(
                    name = it.name!!,
                    parentOrganizationNumber = it.parentOrganizationNumber,
                    organizationNumber = organizationNumber,
                    organizationForm = organizationForm,
                )
        }

    companion object {
        private const val PAGE_SIZE = 500
    }
}

@Profile("dev-gcp", "local")
@Component
class AltinnTilgangerService(
    restTemplateBuilder: RestTemplateBuilder,
    private val authenticatedUserHolder: AuthenticatedUserHolder,
    private val tokenExchangeClient: TokenExchangeClient,

    @Value("\${nais.cluster.name}") private val naisCluster: String,
) : AltinnService {

    private val restTemplate = restTemplateBuilder
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

    override fun hentOrganisasjoner() = hentAltinnTilganger().tilgangerFlatt()

    override fun hentOrganisasjonerBasertPåRettigheter(
        serviceKode: String,
        serviceEdition: String
    ): List<Organisasjon> {
        val altinnTilganger = hentAltinnTilganger()
        val orgnrTilOrg = altinnTilganger.tilgangerFlatt().associateBy { it.organizationNumber }

        return altinnTilganger.tilgangTilOrgNr["${serviceKode}:${serviceEdition}"]?.let { orgnumre ->
            orgnumre.mapNotNull { orgNr -> orgnrTilOrg[orgNr] }
        } ?: emptyList()
    }

    private fun hentAltinnTilganger(): AltinnTilgangerResponse {
        val token = TokenXToken(
            tokenExchangeClient.exchange(
                authenticatedUserHolder.token,
                "$naisCluster:fager:arbeidsgiver-altinn-tilganger"
            ).access_token!!
        )

        val response = restTemplate.exchange(
            RequestEntity
                .method(HttpMethod.POST, "http://arbeidsgiver-altinn-tilganger/altinn-tilganger")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .build(),
            AltinnTilgangerResponse::class.java
        )

        return response.body!! // response != 200 => throws
    }

    private fun flattenUnderOrganisasjoner(
        altinnTilgang: AltinnTilgangerResponse.AltinnTilgang,
        parentOrgNr: String? = null
    ): List<Organisasjon> {
        val parent = Organisasjon(
            name = altinnTilgang.name,
            parentOrganizationNumber = parentOrgNr,
            organizationForm = altinnTilgang.organizationForm,
            organizationNumber = altinnTilgang.orgNr
        )
        val children = altinnTilgang.underenheter.flatMap { flattenUnderOrganisasjoner(it, parent.organizationNumber) }
        return listOf(parent) + children
    }

    private fun AltinnTilgangerResponse.tilgangerFlatt() = hierarki.flatMap {
        flattenUnderOrganisasjoner(it)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class AltinnTilgangerResponse(
    val isError: Boolean,
    val hierarki: List<AltinnTilgang>,
    val orgNrTilTilganger: Map<String, Set<String>>,
    val tilgangTilOrgNr: Map<String, Set<String>>,
) {
    data class AltinnTilgang(
        val orgNr: String,
        val altinn3Tilganger: Set<String>,
        val altinn2Tilganger: Set<String>,
        val underenheter: List<AltinnTilgang>,
        val name: String,
        val organizationForm: String,
    )
}