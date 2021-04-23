package no.nav.permitteringsskjemaapi.config;

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static no.nav.permitteringsskjemaapi.config.Constants.DEFAULT;
import static no.nav.permitteringsskjemaapi.config.Constants.LOCAL;

@Configuration
@Import(TokenGeneratorConfiguration.class)
@Profile({DEFAULT, LOCAL})
public class LocalJWTConfig {
}
