package no.nav.permitteringsskjemaapi.entraID

import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpHeaders
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.spring.oauth2.DefaultOAuth2HttpClient
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI
import java.time.Instant

@Service
class EntraIdKlient(private val config: EntraIdConfig) {
    private val oAuthClient = DefaultOAuth2HttpClient(RestClient.create())
    private val oAuthHeaders = OAuth2HttpHeaders(mapOf("Content-Type" to listOf("application/x-www-form-urlencoded")))

    private var oAuthTokenResponse: OAuth2AccessTokenResponse? = null
    private var tokenHentetTidspunkt: Instant = Instant.MIN

    suspend fun hentToken(scope: String): String {
        if (tokenErGyldig()) {
            return oAuthTokenResponse!!.access_token!!
        }

        val tokenRequest = OAuth2HttpRequest(
            tokenEndpointUrl = URI.create(config.TOKEN_ENDPOINT_URL),
            oAuth2HttpHeaders = oAuthHeaders,
            formParameters = mapOf(
                "client_id" to config.CLIENT_ID,
                "client_secret" to config.CLIENT_SECRET,
                "grant_type" to "client_credentials",
                "scope" to scope
            )
        )
        tokenHentetTidspunkt = Instant.now()
        oAuthTokenResponse = oAuthClient.post(tokenRequest)
        if (oAuthTokenResponse?.accessToken == null) { // linjen ovenfor skal egentlig kaste en exception dersom noe går galt, men vi gjør en ekstra sjekk
            throw OAuth2ClientException("Klarte ikke å hente access token fra EntraID med scope $scope")
        }

        return oAuthTokenResponse!!.accessToken!!
    }

    private fun tokenErGyldig(): Boolean {
        val now = Instant.now()
        val utgårTidspunkt = tokenHentetTidspunkt.plusSeconds(oAuthTokenResponse?.expiresIn!!.toLong() - 5) // legger til et 5 sekunders buffer for å unngå at token er gyldig ved sjekk, men ikke ved utsending av request
        return if (oAuthTokenResponse?.accessToken != null && oAuthTokenResponse?.expiresIn != null)
            now < utgårTidspunkt
        else false
    }
}