package no.nav.permitteringsskjemaapi;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.OAuth2Config;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class LocalOauthConfig {

    private final MockOAuth2Server mockOAuth2Server;

    public LocalOauthConfig() {

        this.mockOAuth2Server = new MockOAuth2Server(
                new OAuth2Config(
                        false,
                        "",
                        new OAuth2TokenProvider(),
                        Set.of(new DefaultOAuth2TokenCallback(
                                "aad",
                                "19097302327",
                                "JWT",
                                List.of("aud-localhost"),
                                Map.of("pid", "19097302327"),
                                3600L
                        ))
                )
        );
    }

    @Bean
    @Primary
    @DependsOn("mockOAuth2Server")
    ProxyAwareResourceRetriever overrideOidcResourceRetriever() {
        return new ProxyAwareResourceRetriever();
    }

    @Bean
    MockOAuth2Server mockOAuth2Server() {
        return mockOAuth2Server;
    }

    @PostConstruct
    void start() {
        mockOAuth2Server.start(9000);
    }

    @PreDestroy
    void shutdown() {
        mockOAuth2Server.shutdown();
    }
}
