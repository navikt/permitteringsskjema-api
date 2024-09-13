package no.nav.permitteringsskjemaapi.altinn
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess


@ActiveProfiles("local")
@RestClientTest(
    AltinnTilgangerService::class,
)
class AltinnTilgangerServiceTest {

    @MockBean
    lateinit var authenticatedUserHolder: AuthenticatedUserHolder

    @MockBean(
        answer = Answers.RETURNS_MOCKS
    )
    lateinit var tokenExchangeClient: TokenExchangeClient

    @Autowired
    lateinit var altinnServer: MockRestServiceServer

    @Autowired
    lateinit var altinnTilgangerService: AltinnTilgangerService

    @Test
    fun `henter organisasjoner fra altinn tilganger proxy` (){
        altinnServer.expect(requestTo("/altinn-tilganger"))
            .andExpect(method(POST))
            .andRespond(
                withSuccess(altinnTilgangerResponse, APPLICATION_JSON)
            )

        val organisasjoner = altinnTilgangerService.hentOrganisasjoner()

        assertTrue(organisasjoner.size == 2)

        val parent = organisasjoner.first( {it.organizationNumber == "810825472"} )
        val underenhet = organisasjoner.first( {it.organizationNumber == "910825496"} )

        assertTrue(parent.name == "Arbeids- og Velferdsetaten")
        assertTrue(underenhet.name == "SLEMMESTAD OG STAVERN REGNSKAP")
        assertTrue(parent.organizationForm == "ORGL")
        assertTrue(underenhet.organizationForm == "BEDR")
    }
}

private val altinnTilgangerResponse = """
    {
      "isError": false,
      "hierarki": [
        {
          "orgNr": "810825472",
          "altinn3Tilganger": [],
          "altinn2Tilganger": [],
          "underenheter": [
            {
              "orgNr": "910825496",
              "altinn3Tilganger": [
                "test-fager"
              ],
              "altinn2Tilganger": [
                "4936:1"
              ],
              "underenheter": [],
              "name": "SLEMMESTAD OG STAVERN REGNSKAP",
              "organizationForm": "BEDR"
            }
          ],
          "name": "Arbeids- og Velferdsetaten",
          "organizationForm": "ORGL"
        }
      ],
      "orgNrTilTilganger": {
        "910825496": [
          "test-fager",
          "4936:1"
        ]
      },
      "tilgangTilOrgNr": {
        "test-fager": [
          "910825496"
        ],
        "4936:1": [
          "910825496"
        ]
      }
    }
""".trimIndent()