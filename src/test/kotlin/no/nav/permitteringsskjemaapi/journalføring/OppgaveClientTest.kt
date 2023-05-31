package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.config.MDCConfig
import no.nav.permitteringsskjemaapi.config.X_CORRELATION_ID
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
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
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.time.LocalDate

private const val OPPGAVESCOPE = "api://oppgave/.default"

@MockBean(MultiIssuerConfiguration::class)
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
    private val skjema = Permitteringsskjema(bedriftNr = "201", varsletNavDato = LocalDate.parse("2020-02-01"))
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
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $mockAzureToken"))
            .andExpect(MockRestRequestMatchers.header("X-Correlation-ID", IsAnything<String>()))
            .andExpect(MockRestRequestMatchers.jsonPath("$.journalpostId").value(journalført.journalpostId))
            .andExpect(MockRestRequestMatchers.jsonPath("$.orgnr").value(skjema.bedriftNr!!))
            .andExpect(MockRestRequestMatchers.jsonPath("$.aktivDato").value("2020-02-01"))
            .andExpect(MockRestRequestMatchers.jsonPath("$.tildeltEnhetsnr").value(journalført.behandlendeEnhet))
            .andExpect(MockRestRequestMatchers.jsonPath("$.beskrivelse").value("Varsel om massepermittering"))
            .andExpect(MockRestRequestMatchers.jsonPath("$.tema").value("PER"))
            .andExpect(MockRestRequestMatchers.jsonPath("$.prioritet").value("HOY"))
            .andExpect(MockRestRequestMatchers.jsonPath("$.oppgavetype").value("VURD_HENV"))
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