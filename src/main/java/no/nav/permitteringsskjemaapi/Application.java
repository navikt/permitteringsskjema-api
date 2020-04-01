package no.nav.permitteringsskjemaapi;

import static no.nav.permitteringsskjemaapi.config.ClusterAwareSpringProfileResolver.profiles;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;

@SpringBootApplication
@EnableKafka
@ConfigurationPropertiesScan("no.nav.permitteringsskjemaapi")
@EnableJwtTokenValidation(ignore = { "org.springframework",
        "springfox.documentation.swagger.web.ApiResourceController" })
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .profiles(profiles())
                .main(Application.class)
                .run(args);
    }
}
