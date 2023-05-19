package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.http.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.SocketException
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.SSLHandshakeException


@Component
class AzureADClient(
    private val azureADProperties: AzureADProperties,
    restTemplateBuilder: RestTemplateBuilder
) {

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
        initialDelayString = "PT1M",
        fixedRateString = "PT1M",
    )
    fun evictionLoop() {
        tokens.filter { it.value.expires }.forEach {
            tokens.remove(it.key)
        }
    }

    private fun hentAccessToken(scope: String): AccessTokenHolder {
            val response: ResponseEntity<TokenResponse> = restTemplate.postForEntity(
                azureADProperties.aadAccessTokenURL,
                HttpEntity(mapOf(
                    "grant_type" to "client_credentials",
                    "client_id" to azureADProperties.clientid,
                    "client_secret" to azureADProperties.azureClientSecret,
                    "resource" to scope,
                ), HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }),
                TokenResponse::class.java
            )
            return AccessTokenHolder(response.body!!)
    }

}

@Configuration
@ConfigurationProperties("azuread")
class AzureADProperties(
    var aadAccessTokenURL: String = "",
    var clientid: String = "",
    var azureClientSecret: String = "",
)

private data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
)

private const val token_expiry_buffer = 120 /*sec*/
private data class AccessTokenHolder(
    val tokenResponse: TokenResponse,
    val createdAt: Instant = Instant.now()
) {
    val expires: Boolean
        get() = Instant.now() > createdAt.plusSeconds((tokenResponse.expires_in + token_expiry_buffer).toLong())
}