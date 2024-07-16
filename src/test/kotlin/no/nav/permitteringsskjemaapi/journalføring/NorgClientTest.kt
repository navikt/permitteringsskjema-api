package no.nav.permitteringsskjemaapi.journalføring

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(
    components = [NorgClient::class],
)
class NorgClientTest {

    @Autowired
    lateinit var norgClient: NorgClient

    @Autowired
    lateinit var server: MockRestServiceServer

    @Test
    fun `henter behandlingsenhet fra norg`() {
        val kommuneNummer = "42"

        server.expect(requestTo("/norg2/api/v1/arbeidsfordeling/enheter/bestmatch"))
            .andExpect(content().json("""{ "geografiskOmraade": "42", "tema": "PER" }""", true))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(norgRespons, APPLICATION_JSON))

        val result = norgClient.hentBehandlendeEnhet(kommuneNummer)

        assertEquals("1337", result)
    }
}

private const val norgRespons = """
[
  {
    "enhetId": 1,
    "navn": "oslo",
    "enhetNr": "4",
    "antallRessurser": 0,
    "status": "foo",
    "orgNivaa": "string",
    "type": "string",
    "organisasjonsnummer": "string",
    "underEtableringDato": "string",
    "aktiveringsdato": "string",
    "underAvviklingDato": "string",
    "nedleggelsesdato": "string",
    "oppgavebehandler": true,
    "versjon": 0,
    "sosialeTjenester": "string",
    "kanalstrategi": "string",
    "orgNrTilKommunaltNavKontor": "string"
  },
  {
    "enhetId": 2,
    "navn": "knapstad",
    "antallRessurser": 0,
    "status": "Aktiv",
    "orgNivaa": "string",
    "type": "string",
    "organisasjonsnummer": "string",
    "underEtableringDato": "string",
    "aktiveringsdato": "string",
    "underAvviklingDato": "string",
    "nedleggelsesdato": "string",
    "oppgavebehandler": true,
    "versjon": 0,
    "sosialeTjenester": "string",
    "kanalstrategi": "string",
    "orgNrTilKommunaltNavKontor": "string"
  },
  {
    "enhetId": 3,
    "navn": "nesodden",
    "enhetNr": "1337",
    "antallRessurser": 0,
    "status": "Aktiv",
    "orgNivaa": "string",
    "type": "string",
    "organisasjonsnummer": "string",
    "underEtableringDato": "string",
    "aktiveringsdato": "string",
    "underAvviklingDato": "string",
    "nedleggelsesdato": "string",
    "oppgavebehandler": true,
    "versjon": 0,
    "sosialeTjenester": "string",
    "kanalstrategi": "string",
    "orgNrTilKommunaltNavKontor": "string"
  }
]
"""