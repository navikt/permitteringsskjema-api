package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpServerErrorException

fun interface NorgClient {
    fun hentBehandlendeEnhet(kommuneNummer: String): String

    companion object {
        const val OSLO_ARBEIDSLIVSENTER_KODE = "0391"
    }
}

/* Dokumentasjon: https://navikt.github.io/norg2/ */
@Service
class NorgClientImpl(
    @Value("\${norg2.baseUrl}") norg2BaseUrl: String,
    restTemplateBuilder: RestTemplateBuilder
): NorgClient {

    private val restTemplate = restTemplateBuilder
        .rootUri(norg2BaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                java.net.SocketException::class.java,
                javax.net.ssl.SSLHandshakeException::class.java,
                HttpServerErrorException.BadGateway::class.java,
                HttpServerErrorException.GatewayTimeout::class.java,
                HttpServerErrorException.ServiceUnavailable::class.java,
            )
        )
        .build()


    override fun hentBehandlendeEnhet(kommuneNummer: String): String {
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

        return norg2ResponseListe.asSequence()
            .filter { it["status"] == "Aktiv" && it.containsKey("enhetNr") }
            .firstOrNull()
            ?.let { it["enhetNr"].toString() }
            ?: OSLO_ARBEIDSLIVSENTER_KODE
    }


    private data class Norg2Request(
        val geografiskOmraade: String,
    ) {
        @Suppress("unused")
        val tema: String = "PER"
    }
}