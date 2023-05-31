package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Component
import java.net.SocketException
import java.time.LocalDate
import javax.net.ssl.SSLHandshakeException

/** Repo: https://github.com/navikt/oppgave
 * Swagger: https://oppgave.dev.intern.nav.no/
 */
@Component
class OppgaveClient(
    restTemplateBuilder: RestTemplateBuilder,
    azureADClient: AzureADClient,
    @Value("\${oppgave.baseUrl}") oppgaveBaseUrl: String,
    @Value("\${oppgave.scope}") oppgaveScope: String,
) {
    private val restTemplate = restTemplateBuilder
        .rootUri(oppgaveBaseUrl)
        .additionalInterceptors(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(azureADClient.getToken(oppgaveScope))
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

    fun lagOppgave(skjema: Permitteringsskjema, journalført: Journalført): String {
        val oppgaveRequest = OppgaveRequest.opprett(skjema, journalført)

        val response = restTemplate.postForObject("/api/v1/oppgaver", oppgaveRequest, OppgaveResponse::class.java)
            ?: throw RuntimeException("null body")

        return response.id
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class OppgaveResponse(
    val id: String,
)

private class OppgaveRequest(
    val journalpostId: String,
    val orgnr: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate,
    val tildeltEnhetsnr: String,
) {
    val beskrivelse: String = "Varsel om massepermittering"
    val tema: String = "PER"
    val prioritet: String = "HOY"
    val oppgavetype: String = "VURD_HENV"

    companion object {
        fun opprett(skjema: Permitteringsskjema, journalført: Journalført) = OppgaveRequest(
            journalpostId = journalført.journalpostId,
            orgnr = skjema.bedriftNr!!,
            aktivDato = skjema.varsletNavDato!!,
            tildeltEnhetsnr = journalført.behandlendeEnhet,
        )
    }
}
