package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.config.MDCConfig
import no.nav.permitteringsskjemaapi.config.X_CORRELATION_ID
import no.nav.permitteringsskjemaapi.permittering.testSkjema
import org.hamcrest.core.IsAnything
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.time.Instant

private const val OPPGAVESCOPE = "api://oppgave/.default"

@RestClientTest(
    components = [
        OppgaveClient::class,
        AzureADClient::class,
    ],
    properties = [
        "oppgave.scope=$OPPGAVESCOPE",
    ]
)
@ImportAutoConfiguration(
    MDCConfig::class
)
class OppgaveClientTest {
    @Autowired
    lateinit var oppgaveClient: OppgaveClient
    private val skjema = testSkjema(bedriftNr = "201", sendtInnTidspunkt = Instant.parse("2020-02-01T12:00:00.00Z"))
    private val journalført =
        Journalført(journalpostId = "101", journalfortAt = "102", kommunenummer = "103", behandlendeEnhet = "104")
    private val mockAzureToken = "lol42"

    @Autowired
    lateinit var server: MockRestServiceServer

    @MockBean
    lateinit var azureADClient: AzureADClient

    @Test
    fun oppgaveOpprettetTest() {
        Mockito.`when`(azureADClient.getToken(OPPGAVESCOPE)).thenReturn(mockAzureToken)

        server.expect(requestTo("/api/v1/oppgaver"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $mockAzureToken"))
            .andExpect(header("X-Correlation-ID", IsAnything()))
            .andExpect(jsonPath("$.journalpostId").value(journalført.journalpostId))
            .andExpect(jsonPath("$.orgnr").value(skjema.bedriftNr))
            .andExpect(jsonPath("$.aktivDato").value("2020-02-01"))
            .andExpect(jsonPath("$.tildeltEnhetsnr").value(journalført.behandlendeEnhet))
            .andExpect(jsonPath("$.beskrivelse").value("Varsel om massepermittering"))
            .andExpect(jsonPath("$.tema").value("PER"))
            .andExpect(jsonPath("$.prioritet").value("HOY"))
            .andExpect(jsonPath("$.oppgavetype").value("VURD_HENV"))
            .andRespond(
                withSuccess(
                    """{"id":1234, "foo":123, "bar":{"baz":123}}""".trimMargin(), MediaType.APPLICATION_JSON
                )
            )
        try {
            MDC.put(X_CORRELATION_ID, "XCORRID123")
            val respons = oppgaveClient.lagOppgave(skjema, journalført)
            assertEquals("1234", respons)
        } finally {
            MDC.remove(X_CORRELATION_ID)
        }
    }

}