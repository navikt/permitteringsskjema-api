package no.nav.permitteringsskjemaapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;

@Configuration
@Import(TokenGeneratorConfiguration.class)
@ConditionalOnLocal
public class LocalJWTConfig {
}
