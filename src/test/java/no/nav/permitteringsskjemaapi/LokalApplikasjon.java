package no.nav.permitteringsskjemaapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.permitteringsskjemaapi.config.Constants.TEST;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan("no.nav.permitteringsskjemaapi")
@EnableJwtTokenValidation(ignore = { "org.springframework", "springfox.documentation.swagger.web.ApiResourceController" })
// Hvis ikke vet ikke Spring forskjell på LokalApplikasjon og Application
@Profile(TEST)
public class LokalApplikasjon {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .main(LokalApplikasjon.class)
                .run(args);
    }
}
