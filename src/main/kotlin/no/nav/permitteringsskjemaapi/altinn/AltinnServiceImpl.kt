package no.nav.permitteringsskjemaapi.altinn

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
@Profile("dev-gcp", "prod-gcp")
class AltinnServiceImpl(
    private val altinnConfig: AltinnConfig,
    private val restTemplate: RestTemplate
) : AltinnService {

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