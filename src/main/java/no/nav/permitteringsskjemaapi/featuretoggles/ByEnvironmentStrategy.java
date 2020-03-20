package no.nav.permitteringsskjemaapi.featuretoggles;


import no.finn.unleash.strategy.Strategy;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidstaker.PermitteringMeldingLoggingProdusent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.boot.conditionals.EnvUtil.LOCAL;

@Component
public class ByEnvironmentStrategy implements Strategy {

    private final String environment;
    public static final List<String> MILJOER = Arrays.asList("dev-fss", "prod-fss");

    public ByEnvironmentStrategy(Environment environment) {
        this.environment = Stream.of(environment.getActiveProfiles()).filter(MILJOER::contains).findFirst().orElse("dev-fss");
    }

    @Override
    public String getName() {
        return "byEnvironment";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return Optional.ofNullable(parameters)
                .map(map -> map.get("miljø"))
                .map(env -> asList(env.split(",")).contains(environment))
                .orElse(false);
    }

    String getEnvironment() {
        return environment;
    }
}
