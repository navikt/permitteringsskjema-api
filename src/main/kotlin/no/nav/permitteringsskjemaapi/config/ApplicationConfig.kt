package no.nav.permitteringsskjemaapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@EnableScheduling
@Configuration
@ConfigurationPropertiesScan("no.nav.permitteringsskjemaapi")
@EnableOAuth2Client(cacheEnabled = true)
@Import(TokenSupportJwtConfig::class)
class ApplicationConfig {

    private val log = logger()

    @Bean
    fun bearerTokenRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate = restTemplateBuilder
        .additionalInterceptors(
            ClientHttpRequestInterceptor { request, body, execution ->
                val accessTokenResponse =
                    oAuth2AccessTokenService.getAccessToken(clientConfigurationProperties.registration["altinn-rettigheter-client"])
                request.headers.setBearerAuth(accessTokenResponse.accessToken)
                request.headers["x-consumer-id"] = "permitteringsskjema-api"
                execution.execute(request, body)
            },
        )
        .build()

    /**
     * log basic info om request response via resttemplate
     */
    @Bean
    fun loggingInterceptorCustomizer() = RestTemplateCustomizer { restTemplate: RestTemplate ->
        restTemplate.interceptors.add(
            ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
                log.info("RestTemplate.request: {} {}{}", request.method, request.uri.host, request.uri.path)
                val response = execution.execute(request, body!!)
                log.info("RestTemplate.response: {} {}", response.statusCode, response.headers.contentLength)
                response
            })
    }

    /**
     * log basic info om request response på våre endepunkter
     */
    @Bean
    fun requestResponseLoggingFilter() = object : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            chain: FilterChain
        ) {
            try {
                log.info("servlet.request {} {}", request.method, request.requestURI)
                chain.doFilter(request, response)
            } finally {
                log.info(
                    "servlet.response {} {} => {}",
                    request.method, request.requestURI, HttpStatus.resolve(response.status)
                )
            }
        }

        override fun shouldNotFilter(request: HttpServletRequest): Boolean {
            return request.requestURI.contains("internal/actuator")
        }
    }
}

inline fun <reified T : Any> T.logger(): Logger = LoggerFactory.getLogger(this::class.java)