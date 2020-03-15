package no.nav.permitteringsskjemaapi;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwtTokenValidation(ignore={"org.springframework", "springfox.documentation.swagger.web.ApiResourceController"})
public class PermitteringsskjemaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermitteringsskjemaApiApplication.class, args);
    }

}
