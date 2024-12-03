package no.nav.permitteringsskjemaapi.entraID

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.util.multiValueMapOf
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.http.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.SocketException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.SSLHandshakeException


@Component
class EntraIdKlient(
    private val entraIdConfig: EntraIdConfig,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val log = logger()

    private val restTemplate = restTemplateBuilder.additionalInterceptors(
        retryInterceptor(
            3,
            250L,
            SocketException::class.java,
            SSLHandshakeException::class.java,
        )
    ).build()

    private val tokens = ConcurrentHashMap<String, AccessTokenHolder>()

    fun getToken(scope: String) = tokens.computeIfAbsent(scope) {
        hentAccessToken(it)
    }.tokenResponse.access_token

    @Scheduled(
        initialDelayString = "PT10S",
        fixedRateString = "PT10S",
    )
    fun evictionLoop() {
        tokens.filter {
            it.value.hasExpired
        }.forEach {
            tokens.remove(it.key)
        }
    }

    private fun hentAccessToken(scope: String): AccessTokenHolder {
        try {
            val response: ResponseEntity<TokenResponse> = restTemplate.postForEntity(
                entraIdConfig.aadAccessTokenURL,
                HttpEntity(
                    multiValueMapOf(
                        "grant_type" to "client_credentials",
                        "client_id" to entraIdConfig.clientid,
                        "client_secret" to entraIdConfig.azureClientSecret,
                        "scope" to scope,
                    ),
                    HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
                ),
                TokenResponse::class.java
            )
            return AccessTokenHolder(response.body!!)
        } catch (e: Exception) {
            log.error("feil ved henting av Azure AD maskin-maskin access token", e)
            throw e
        }
    }
}

@Configuration
@ConfigurationProperties("azuread")
class EntraIdConfig(
    var aadAccessTokenURL: String = "",
    var clientid: String = "",
    var azureClientSecret: String = "",
)

private data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
)

private const val token_expiry_buffer = 10 /*sec*/

private data class AccessTokenHolder(
    val tokenResponse: TokenResponse,
    val createdAt: Instant = Instant.now()
) {
    val hasExpired: Boolean
        get() = Instant.now() > createdAt.plusSeconds((tokenResponse.expires_in + token_expiry_buffer).toLong())
}