package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import java.net.SocketException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLHandshakeException

interface DokgenClient {
    fun genererPdf(skjema: Permitteringsskjema): ByteArray
    fun genererTrukketPdf(skjema: Permitteringsskjema): ByteArray
}

@Component
class DokgenClientImpl(
    @Value("\${permittering-dokgen.baseUrl}") dokgenBaseUrl: String,
    restTemplateBuilder: RestTemplateBuilder
) : DokgenClient {
    private val restTemplate = restTemplateBuilder
        .rootUri(dokgenBaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                SocketException::class.java,
                SSLHandshakeException::class.java,
                HttpServerErrorException.BadGateway::class.java,
                HttpServerErrorException.GatewayTimeout::class.java,
                HttpServerErrorException.ServiceUnavailable::class.java,
            )
        )
        .build()

    override fun genererPdf(skjema: Permitteringsskjema): ByteArray {
        val payload = TemplateVariables(
            bedriftsnummer = skjema.bedriftNr,
            bedriftNavn = skjema.bedriftNavn,
            sendtInnTidspunkt = skjema.sendtInnTidspunkt.atZone(OSLO_ZONE).format(ISO_OFFSET_FORMAT),
            type = skjema.type,
            kontaktNavn = skjema.kontaktNavn,
            kontaktTlf = skjema.kontaktTlf,
            kontaktEpost = skjema.kontaktEpost,
            startDato = skjema.startDato,
            sluttDato = skjema.sluttDato,
            fritekst = skjema.fritekst,
            antallBerorte = skjema.antallBerørt
        )
        return postPdf("/template/permittering/create-pdf", payload)
    }

    override fun genererTrukketPdf(skjema: Permitteringsskjema): ByteArray {
        val payload = TemplateVariables(
            bedriftsnummer = skjema.bedriftNr,
            bedriftNavn = skjema.bedriftNavn,
            sendtInnTidspunkt = skjema.sendtInnTidspunkt.atZone(OSLO_ZONE).format(ISO_OFFSET_FORMAT),
            type = skjema.type,
            kontaktNavn = skjema.kontaktNavn,
            kontaktTlf = skjema.kontaktTlf,
            kontaktEpost = skjema.kontaktEpost,
            startDato = skjema.startDato,
            sluttDato = skjema.sluttDato,
            fritekst = skjema.fritekst,
            antallBerorte = skjema.antallBerørt,
            trukketTidspunkt = (skjema.trukketTidspunkt ?: Instant.now())
                .atZone(OSLO_ZONE)
                .format(ISO_OFFSET_FORMAT),
            )
        return postPdf("/template/permittering-trukket/create-pdf", payload)
    }

    private fun postPdf(path: String, payload: Any): ByteArray {
        val bytes = restTemplate.postForObject(path, payload, ByteArray::class.java)
            ?: error("Dokgen returnerte tom body for $path")
        // Valider PDF-header
        check(bytes.size >= 4 && bytes.sliceArray(0..3).contentEquals("%PDF".toByteArray())) {
            "Body fra dokgen mangler PDF-header '%PDF'. Html-feil?"
        }
        return bytes
    }

    private data class TemplateVariables(
        val bedriftsnummer: String,
        val bedriftNavn: String,

        val sendtInnTidspunkt: String,

        val type: SkjemaType,
        val kontaktNavn: String,
        val kontaktTlf: String,
        val kontaktEpost: String,

        @JsonFormat(pattern = "yyyy-MM-dd")
        val startDato: LocalDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        val sluttDato: LocalDate?,

        val fritekst: String,
        val antallBerorte: Int = 0,
        val trukketTidspunkt: String? = null,
    )

    private companion object {
        val OSLO_ZONE: ZoneId = ZoneId.of("Europe/Oslo")
        val ISO_OFFSET_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
