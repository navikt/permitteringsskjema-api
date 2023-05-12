package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

/* Dokumentasjon: https://navikt.github.io/norg2/ */
@Service
class NorgService(
    @Value("\${norg2.baseUrl}") norg2BaseUrl: String,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val log = logger()

    private val restTemplate = restTemplateBuilder
        .rootUri(norg2BaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                java.net.SocketException::class.java,
                javax.net.ssl.SSLHandshakeException::class.java,
            )
        )
        .build()


    fun hentBehandlendeEnhet(kommuneNummer: String?): String? {
        if (kommuneNummer == null) {
            return OSLO_ARBEIDSLIVSENTER_KODE
        }

        val norg2ResponseListe = try {
            restTemplate.exchange(
                "/norg2/api/v1/arbeidsfordeling/enheter/bestmatch",
                HttpMethod.POST,
                HttpEntity(Norg2Request(kommuneNummer)),
                object : ParameterizedTypeReference<List<Map<String, Any>>>() {}
            ).body.orEmpty()
        } catch (e: Exception) {
            throw RuntimeException("Hente behandlendeEnhet for $kommuneNummer feilet", e)
        }

        // TODO: Burde vi returnere Oslo hvis vi ikke finner noe?
        return norg2ResponseListe.asSequence()
            .filter { it["status"] == "Aktiv" && it.containsKey("enhetNr") }
            .firstOrNull()
            ?.let { it["enhetNr"].toString() }
    }

    companion object {
        const val OSLO_ARBEIDSLIVSENTER_KODE = "0391"
    }

    data class Norg2Request(
        val geografiskOmraade: String,
    ) {
        val tema: String = "PER"
    }
}