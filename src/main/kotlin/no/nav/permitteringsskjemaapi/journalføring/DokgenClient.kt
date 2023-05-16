package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import java.net.SocketException
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.net.ssl.SSLHandshakeException

@Component
class DokgenClient(
    @Value("\${permittering-dokgen.baseUrl}") dokgenBaseUrl: String?,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val restTemplate = restTemplateBuilder
        .rootUri(dokgenBaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                SocketException::class.java,
                SSLHandshakeException::class.java,
            )
        )
        .build()

    fun genererPdf(skjema: Permitteringsskjema): ByteArray {
        val templateVariables = TemplateVariables(
            bedriftsnummer = skjema.bedriftNr!!,
            bedriftNavn = skjema.bedriftNavn!!,
            sendtInnTidspunkt = skjema.sendtInnTidspunkt!!,
            type = skjema.type!!,
            kontaktNavn = skjema.kontaktNavn!!,
            kontaktTlf = skjema.kontaktTlf!!,
            kontaktEpost = skjema.kontaktEpost!!,
            startDato = skjema.startDato!!,
            sluttDato = skjema.sluttDato,
            fritekst = skjema.fritekst!!,
            antallBerorte = skjema.antallBerørt!!
        )
        val bytes = restTemplate.postForObject("/template/permittering/create-pdf", templateVariables, ByteArray::class.java)!!
        check(bytes.sliceArray(0..3).contentEquals("%PDF".toByteArray())) {
            "Body fra dokgen mangler PDF-header '%PDF'. Html feilmelding?"
        }
        return bytes
    }

    private data class TemplateVariables(
        val bedriftsnummer: String,
        val bedriftNavn: String,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        val sendtInnTidspunkt: Instant,
        val type: PermitteringsskjemaType,
        val kontaktNavn: String,
        val kontaktTlf: String,
        val kontaktEpost: String,

        @JsonFormat(pattern = "yyyy-MM-dd")
        val startDato: LocalDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        val sluttDato: LocalDate?,
        val fritekst: String,
        val antallBerorte: Int = 0,
    )
}
