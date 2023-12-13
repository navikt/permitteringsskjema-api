package no.nav.permitteringsskjemaapi

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.context.annotation.*

@Configuration
@Profile("mockoauth")
class LocalOauthConfig {
    private val mockOAuth2Server: MockOAuth2Server = MockOAuth2Server(
        OAuth2Config(
            interactiveLogin = false,
            loginPagePath = "",
            tokenProvider = OAuth2TokenProvider(),
            tokenCallbacks = setOf(
                DefaultOAuth2TokenCallback(
                    issuerId = "aad",
                    subject = "19097302327",
                    typeHeader = "JWT",
                    audience = listOf("aud-localhost"),
                    claims = mapOf("pid" to "19097302327"),
                    expiry = 3600L
                )
            )
        )
    )

    @Bean
    @Primary
    @DependsOn("mockOAuth2Server")
    fun overrideOidcResourceRetriever(): ProxyAwareResourceRetriever {
        return ProxyAwareResourceRetriever()
    }

    @Bean
    fun mockOAuth2Server(): MockOAuth2Server {
        return mockOAuth2Server
    }

    @PostConstruct
    fun start() {
        mockOAuth2Server.start(9000)
    }

    @PreDestroy
    fun shutdown() {
        mockOAuth2Server.shutdown()
    }
}