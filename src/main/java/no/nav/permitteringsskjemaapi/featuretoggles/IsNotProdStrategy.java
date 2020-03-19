package no.nav.permitteringsskjemaapi.featuretoggles;


import no.finn.unleash.strategy.Strategy;
import no.nav.permitteringsskjemaapi.config.ClusterAwareSpringProfileResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IsNotProdStrategy implements Strategy {

    private final String env;

    public IsNotProdStrategy() {
        this.env = ClusterAwareSpringProfileResolver.profiles()[0];
    }

    @Override
    public String getName() {
        return "isNotProd";
    }

    @Override
    public boolean isEnabled(Map<String, String> map) {
        return !isProd(this.env);
    }

    private boolean isProd(String environment) {
        return "prod-fss".equalsIgnoreCase(environment);
    }

}