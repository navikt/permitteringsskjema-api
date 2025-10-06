package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.SocketException
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import javax.net.ssl.SSLHandshakeException

/** Repo: https://github.com/navikt/oppgave
 * Swagger: https://oppgave.dev.intern.nav.no/
 */

fun interface OppgaveClient {
    fun lagOppgave(
        skjema: Permitteringsskjema,
        journalført: Journalført,
        beskrivelse: String,
    ): String
}

@Component
class OppgaveClientImpl(
    restTemplateBuilder: RestTemplateBuilder,
    entraIdKlient: EntraIdKlient,
    @Value("\${oppgave.baseUrl}") oppgaveBaseUrl: String,
    @Value("\${oppgave.scope}") oppgaveScope: String,
) : OppgaveClient {
    private val restTemplate = restTemplateBuilder
        .rootUri(oppgaveBaseUrl)
        .connectTimeout(Duration.ofSeconds(2))
        .readTimeout(Duration.ofSeconds(60))
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                SocketException::class.java,
                SSLHandshakeException::class.java,
                ResourceAccessException::class.java,
                HttpClientErrorException.Unauthorized::class.java,
                HttpServerErrorException.BadGateway::class.java,
                HttpServerErrorException.GatewayTimeout::class.java,
                HttpServerErrorException.ServiceUnavailable::class.java,
            ),
            { request, body, execution ->
                request.headers.setBearerAuth(entraIdKlient.getToken(oppgaveScope))
                execution.execute(request, body)
            },
        )
        .build()

    override fun lagOppgave(
        skjema: Permitteringsskjema,
        journalført: Journalført,
        beskrivelse: String,
    ): String {
        val oppgaveRequest = OppgaveRequest.opprett(skjema, journalført, beskrivelse)

        val response = restTemplate.postForObject("/api/v1/oppgaver", oppgaveRequest, OppgaveResponse::class.java)
            ?: throw RuntimeException("null body")

        return response.id
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class OppgaveResponse(
    val id: String,
)

@Suppress("unused")
private class OppgaveRequest(
    val journalpostId: String,
    val orgnr: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate,
    val tildeltEnhetsnr: String,
    val beskrivelse: String,
) {
    val tema: String = "PER"
    val prioritet: String = "HOY"
    val oppgavetype: String = "VURD_HENV"

    companion object {
        fun opprett(
            skjema: Permitteringsskjema,
            journalført: Journalført,
            beskrivelse: String,
        ) = OppgaveRequest(
            journalpostId = journalført.journalpostId,
            orgnr = skjema.bedriftNr,
            // Bruk Oslo-tid for å tolke Instant til lokal dato
            aktivDato = skjema.sendtInnTidspunkt.let { LocalDate.ofInstant(it, ZoneId.of("Europe/Oslo")) },
            tildeltEnhetsnr = journalført.behandlendeEnhet,
            beskrivelse = beskrivelse,
        )
    }
}
