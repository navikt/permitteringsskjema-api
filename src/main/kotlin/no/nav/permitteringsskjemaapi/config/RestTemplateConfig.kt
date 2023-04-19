package no.nav.permitteringsskjemaapi.config;


import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate bearerTokenRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        return restTemplateBuilder
                .additionalInterceptors(
                        bearerTokenInterceptor(
                                clientConfigurationProperties.getRegistration().get("altinn-rettigheter-client"), oAuth2AccessTokenService))
                .build();
    }

    private ClientHttpRequestInterceptor bearerTokenInterceptor(
            ClientProperties clientProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        return (httpRequest, bytes, clientHttpRequestExecution) ->  {
            OAuth2AccessTokenResponse accessTokenResponse = oAuth2AccessTokenService.getAccessToken(clientProperties);
            httpRequest.getHeaders().setBearerAuth(accessTokenResponse.getAccessToken());
            httpRequest.getHeaders().set("x-consumer-id", "permitteringsskjema-api");
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
    }
}
