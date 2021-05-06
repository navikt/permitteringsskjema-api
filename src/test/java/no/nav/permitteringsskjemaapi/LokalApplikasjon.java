package no.nav.permitteringsskjemaapi;

import static no.nav.permitteringsskjemaapi.config.ClusterAwareSpringProfileResolver.profiles;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan("no.nav.permitteringsskjemaapi")
@EnableJwtTokenValidation(ignore = { "org.springframework",
        "springfox.documentation.swagger.web.ApiResourceController" })
public class LokalApplikasjon {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .profiles(profiles())
                .main(LokalApplikasjon.class)
                .run(args);
    }
}
