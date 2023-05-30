package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import java.net.SocketException
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.net.ssl.SSLHandshakeException

interface DokarkivClient {
    fun opprettjournalPost(
        skjema: Permitteringsskjema,
        behandlendeEnhet: String,
        dokumentPdfAsBytes: ByteArray,
    ): String
}

/**
 * - https://confluence.adeo.no/pages/viewpage.action?pageId=371033358
 * - https://confluence.adeo.no/display/BOA/Arkivering+i+fagarkivet
 */
@Component
@Profile("!prod-gcp")
class DokarkivClientImpl(
    @Value("\${dokarkiv.scope}") dokarkivScope: String,
    @Value("\${dokarkiv.baseUrl}") dokarkivBaseUrl: String,
    val azureADClient: AzureADClient,
    restTemplateBuilder: RestTemplateBuilder,
) : DokarkivClient {
    private val log = logger()

    init {
        log.info("Dokarkiv url: {}", dokarkivBaseUrl)
    }

    private val restTemplate = restTemplateBuilder
        .rootUri(dokarkivBaseUrl)
        .errorHandler(object: DefaultResponseErrorHandler() {
            override fun hasError(response: ClientHttpResponse): Boolean {
                val statusCode = response.statusCode
                val isValidStatusCode = statusCode.is2xxSuccessful
                        || statusCode.isSameCodeAs(HttpStatusCode.valueOf(409))
                return !isValidStatusCode
            }
        })
        .additionalInterceptors(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(azureADClient.getToken(dokarkivScope))
                execution.execute(request, body)
            },
            retryInterceptor(
                3,
                250L,
                SocketException::class.java,
                SSLHandshakeException::class.java,
            )
        )
        .build()

    /**
     * https://confluence.adeo.no/display/BOA/opprettJournalpost
     */
    override fun opprettjournalPost(
        skjema: Permitteringsskjema,
        behandlendeEnhet: String,
        dokumentPdfAsBytes: ByteArray,
    ) = restTemplate.postForObject(
        "/journalpost?forsoekFerdigstill=true",
        Journalpost(
            bruker = Bruker(skjema.bedriftNr!!),
            datoMottatt = LocalDate.ofInstant(skjema.sendtInnTidspunkt!!, ZoneId.of("Europe/Oslo")),
            avsenderMottaker = Avsender(skjema.bedriftNr!!, skjema.bedriftNavn!!),
            eksternReferanseId = "PRM-${skjema.id!!}",
            journalfoerendeEnhet = behandlendeEnhet,
            pdf = String(Base64.getEncoder().encode(dokumentPdfAsBytes)),
        ),
        DokarkivResponse::class.java
    )!!.journalpostId


    @Suppress("unused")
    private data class Journalpost(
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoMottatt: LocalDate,
        val bruker: Bruker,
        val avsenderMottaker: Avsender,
        val eksternReferanseId: String,
        val journalfoerendeEnhet: String,
        val pdf : String,
    ) {
        val journalposttype = "INNGAAENDE"
        val kanal = "NAV_NO"
        val tema = "PER"
        val tittel =
            "Arbeidsgivers meldeplikt til NAV ved masseoppsigelser, permitteringer uten lønn og innskrenking i arbeidstiden"
        val sak: Sak = Sak()
        val dokumenter: List<Dokument> = listOf(Dokument(pdf))

    }


    @Suppress("unused")
    private data class Bruker(
        val id: String
    ) {
        val idType = "ORGNR"
    }


    @Suppress("unused")
    private class Sak {
        val sakstype = "GENERELL_SAK"
    }


    @Suppress("unused")
    private class Dokument(fysiskDokument: String) {
        val brevkode = "NAV 76-08.03"
        val tittel =
            "Arbeidsgivers meldeplikt til NAV ved masseoppsigelser, permitteringer uten lønn og innskrenking i arbeidstiden"
        val dokumentVarianter: List<DokumentVariant> = listOf(DokumentVariant(fysiskDokument))
    }


    @Suppress("unused")
    private data class DokumentVariant(val fysiskDokument: String) {
        val filtype: String = "PDFA"
        val variantformat: String = "ARKIV"
    }


    @Suppress("unused")
    private data class Avsender(
        val id: String,
        val navn: String,
    ) {
        val idType = "ORGNR"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class DokarkivResponse(val journalpostId: String)
}

@Component
@Profile("prod-gcp")
class DokarkivClientStub : DokarkivClient {
    private val log = logger()
    override fun opprettjournalPost(
        skjema: Permitteringsskjema,
        behandlendeEnhet: String,
        dokumentPdfAsBytes: ByteArray
    ): String {
        log.info("dokarkiv-integration disabled. would have created journalpost for schema {}", skjema.id)
        return "stub-dokarkiv-id"
    }
}
