package no.nav.permitteringsskjemaapi;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.OAuth2Config;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import no.nav.security.token.support.spring.test.MockLoginController;
import no.nav.security.token.support.spring.test.MockOAuth2ServerAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Configuration
@Import(MockLoginController.class)
public class LocalOauthConfig {

    private final Logger log = LoggerFactory.getLogger(MockOAuth2ServerAutoConfiguration.class);
    private final MockOAuth2Server mockOAuth2Server;

    public LocalOauthConfig() {

        DefaultOAuth2TokenCallback callback = new DefaultOAuth2TokenCallback(
                "aad",
                "19097302327",
                "JWT",
                List.of("aud-localhost"),
                Map.of("pid", "19097302327"),
                3600L
        );

        this.mockOAuth2Server = new MockOAuth2Server(
                new OAuth2Config(
                        false,
                        "",
                        new OAuth2TokenProvider(),
                        Set.of(callback)
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
        try {
            int port = 9000;
            log.debug("starting mock oauth2 server on port " + port);
            mockOAuth2Server.start(port);
        } catch (IOException e) {
            log.error("could not register and start MockOAuth2Server");
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    void shutdown() throws IOException {
        log.debug("shutting down the mock oauth2 server.");
        mockOAuth2Server.shutdown();
    }
}
