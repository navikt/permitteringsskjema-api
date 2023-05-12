package no.nav.permitteringsskjemaapi

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mockoauth")
class LocalOauthConfig {
    private val mockOAuth2Server: MockOAuth2Server = MockOAuth2Server(
        OAuth2Config(
            false,
            "",
            OAuth2TokenProvider(),
            setOf(
                DefaultOAuth2TokenCallback(
                    "aad",
                    "19097302327",
                    "JWT", listOf("aud-localhost"),
                    mapOf("pid" to "19097302327"),
                    3600L
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