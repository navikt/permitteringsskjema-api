package no.nav.permitteringsskjemaapi.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;
import no.nav.foreldrepenger.boot.conditionals.Cluster;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnClusters;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import no.nav.permitteringsskjemaapi.config.ClusterAwareSpringProfileResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "permitteringsskjema-api";

    @Bean
    @ConditionalOnClusters(clusters = {Cluster.DEV_FSS, Cluster.PROD_FSS})
    public Unleash initializeUnleash( @Value(
            "${unleash.unleash-uri}") String unleashUrl,
                                      ByEnvironmentStrategy byEnvironmentStrategy,
                                      IsNotProdStrategy isNotProdStrategy)
            {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy,
                isNotProdStrategy
        );
    }

    @ConditionalOnLocal
    @Bean
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable("permittering.frontend");
        return fakeUnleash;
    }
}
