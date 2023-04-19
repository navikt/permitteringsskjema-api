package no.nav.permitteringsskjemaapi.config

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
class RestTemplateConfig {
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

}