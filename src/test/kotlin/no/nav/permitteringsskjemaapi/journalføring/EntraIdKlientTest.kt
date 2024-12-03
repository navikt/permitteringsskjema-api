package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.entraID.EntraIdConfig
import no.nav.permitteringsskjemaapi.util.multiValueMapOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(
    components = [
        EntraIdKlient::class,
        EntraIdConfig::class,
    ],
    properties = [
        "azuread.aadAccessTokenURL=/some-azure-ad-endpoint",
        "azuread.clientid=fake-client-id",
        "azuread.azureClientSecret=fake-client-secret",
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

        /** Example response taken from https://learn.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#successful-response-2 */
        val exampleResponse = """
            {
                "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5HVEZ2ZEstZnl0aEV1Q...",
                "token_type": "Bearer",
                "expires_in": 3599,
                "scope": "https%3A%2F%2Fgraph.microsoft.com%2Fmail.read",
                "refresh_token": "AwABAAAAvPM1KaPlrEqdFSBzjqfTGAMxZGUTdM0t4B4...",
                "id_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJhdWQiOiIyZDRkMTFhMi1mODE0LTQ2YTctOD..."
            }
        """.trimIndent()

        server.expect(requestTo("/some-azure-ad-endpoint"))
            .andExpect(content().formData(multiValueMapOf(
                "grant_type" to "client_credentials",
                "client_secret" to "fake-client-secret",
                "client_id" to "fake-client-id",
                "scope" to "some_scope",
            )))
            .andRespond(withSuccess(exampleResponse, MediaType.APPLICATION_JSON))

        val token = entraIdKlient.getToken(scope)

        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5HVEZ2ZEstZnl0aEV1Q...", token)
    }

}