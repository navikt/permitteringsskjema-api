package no.nav.permitteringsskjemaapi.entraID

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException


@Component
class EntraIdKlient(
    @Value("\${nais.token.endpoint}") private val tokenEndpoint: String,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val log = logger()

    private val restTemplate = restTemplateBuilder.additionalInterceptors(
        retryInterceptor(
            3,
            250L,
            SocketException::class.java,
            SSLHandshakeException::class.java,
            HttpServerErrorException.BadGateway::class.java,
            HttpServerErrorException.GatewayTimeout::class.java,
            HttpServerErrorException.ServiceUnavailable::class.java,
        )
    ).build()

    fun getToken(scope: String): String {
        return try {
            restTemplate.postForEntity(
                tokenEndpoint,
                mapOf(
                    "identity_provider" to "azuread",
                    "target" to scope
                ),
                TokenResponse::class.java
            ).body!!.accessToken
        } catch (e: Exception) {
            log.error("feil ved henting av m2m access token", e)
            throw e
        }
    }
}

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String? = null,
    @JsonProperty("expires_in")
    val expiresInSeconds: Int,
)
