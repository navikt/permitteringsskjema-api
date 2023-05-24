package no.nav.permitteringsskjemaapi.journalføring

import jakarta.inject.Inject
import no.nav.permitteringsskjemaapi.util.multiValueMapOf
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@MockBean(MultiIssuerConfiguration::class)
@RestClientTest(
    components = [
        AzureADClient::class,
        AzureADProperties::class,
    ],
    properties = [
        "azuread.aadAccessTokenURL=/some-azure-ad-endpoint",
        "azuread.clientid=fake-client-id",
        "azuread.azureClientSecret=fake-client-secret",
    ]
)
class AzureADClientTest {
    @Autowired
    lateinit var azureADClient: AzureADClient

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
                "resource" to "some_scope",
            )))
            .andRespond(withSuccess(exampleResponse, MediaType.APPLICATION_JSON))

        val token = azureADClient.getToken(scope)

        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5HVEZ2ZEstZnl0aEV1Q...", token)
    }

}