package no.nav.permitteringsskjemaapi.entraID

import no.nav.permitteringsskjemaapi.util.multiValueMapOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

@RestClientTest(
    components = [
        EntraIdKlient::class,
    ],
    properties = [
        "nais.token.endpoint=/some-azure-ad-endpoint",
    ]
)
class EntraIdKlientTest {
    @Autowired
    lateinit var entraIdKlient: EntraIdKlient

    @Autowired
    lateinit var server: MockRestServiceServer

    @Test
    fun encodesRequest() {
        val scope = "some_scope"

        /** Example response taken from https://doc.nais.io/auth/entra-id/how-to/consume-m2m/ */
        val exampleResponse = """
            {
                "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5HVEZ2ZEstZnl0aEV1Q...",
                "expires_in": 3599,
                "token_type": "Bearer"
            }
        """.trimIndent()

        server.expect(MockRestRequestMatchers.requestTo("/some-azure-ad-endpoint"))
            .andExpect(
                MockRestRequestMatchers.content().json("""
                    {
                        "identity_provider": "azuread",
                        "target": "some_scope"
                    }
                """))
            .andRespond(MockRestResponseCreators.withSuccess(exampleResponse, MediaType.APPLICATION_JSON))

        val token = entraIdKlient.getToken(scope)

        Assertions.assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5HVEZ2ZEstZnl0aEV1Q...", token)
    }

}