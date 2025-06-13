package no.nav.permitteringsskjemaapi.altinn
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.tokenx.TokenXToken
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess


@ActiveProfiles("local")
@RestClientTest(
    AltinnService::class,
)
class AltinnServiceTest {

    @MockitoBean
    lateinit var authenticatedUserHolder: AuthenticatedUserHolder

    @MockitoBean
    lateinit var tokenXClient: TokenExchangeClient

    @Autowired
    lateinit var altinnServer: MockRestServiceServer

    @Autowired
    lateinit var altinnService: AltinnService

    @Test
    fun `henter organisasjoner fra altinn tilganger proxy` (){
        `when`(authenticatedUserHolder.token).thenReturn("user_token")
        `when`(tokenXClient.exchange("user_token", "local:fager:arbeidsgiver-altinn-tilganger"))
            .thenReturn(TokenXToken(access_token = "obo_tokenx_token"))


        altinnServer.expect(requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger"))
            .andExpect(method(POST))
            .andRespond(
                withSuccess(altinnTilgangerResponse, APPLICATION_JSON)
            )

        val tilganger = altinnService.hentAltinnTilganger()

        val parent = tilganger.hierarki.first { it.orgnr == "810825472" }
        val underenhet = tilganger.hierarki.first().underenheter.first { it.orgnr == "910825496" }

        assertTrue(parent.navn == "Arbeids- og Velferdsetaten")
        assertTrue(underenhet.navn == "SLEMMESTAD OG STAVERN REGNSKAP")
        assertTrue(parent.organisasjonsform == "ORGL")
        assertTrue(underenhet.organisasjonsform == "BEDR")
    }
}

//language=JSON
private val altinnTilgangerResponse = """
    {
      "isError": false,
      "hierarki": [
        {
          "orgnr": "810825472",
          "altinn3Tilganger": [],
          "altinn2Tilganger": [],
          "underenheter": [
            {
              "orgnr": "910825496",
              "altinn3Tilganger": [
                "test-fager"
              ],
              "altinn2Tilganger": [
                "4936:1"
              ],
              "underenheter": [],
              "navn": "SLEMMESTAD OG STAVERN REGNSKAP",
              "organisasjonsform": "BEDR"
            }
          ],
          "navn": "Arbeids- og Velferdsetaten",
          "organisasjonsform": "ORGL"
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