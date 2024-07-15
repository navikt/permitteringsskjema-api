package no.nav.permitteringsskjemaapi.altinn

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.exchange

interface AltinnService {
    fun hentOrganisasjoner(): List<AltinnOrganisasjon>
    fun hentOrganisasjonerBasertPåRettigheter(serviceKode: String, serviceEdition: String): List<AltinnOrganisasjon>
}

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
    )

    override fun hentOrganisasjonerBasertPåRettigheter(
        serviceKode: String,
        serviceEdition: String
    ) = getFromAltinn(
        "${altinnConfig.altinnProxyUrl}reportees/?ForceEIAuthentication&\$filter=Type+ne+'Person'+and+Status+eq+'Active'&serviceCode=$serviceKode&serviceEdition=$serviceEdition",
    )

    fun getFromAltinn(
        url: String,
    ): List<AltinnOrganisasjon> {
        val response: MutableSet<AltinnOrganisasjon> = HashSet()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            val pageQuery = "&\$top=$PAGE_SIZE&\$skip=${pageNumber * PAGE_SIZE}"
            hasMore = try {
                restTemplate.exchange<List<AltinnOrganisasjon>>("$url$pageQuery", HttpMethod.GET, null).body?.let {
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

    companion object {
        private const val PAGE_SIZE = 500
    }
}
