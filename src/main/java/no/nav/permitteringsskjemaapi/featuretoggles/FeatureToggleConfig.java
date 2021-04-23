package no.nav.permitteringsskjemaapi.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static no.nav.permitteringsskjemaapi.config.Constants.*;

@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "permitteringsskjema-api";
    private final static String UNLEASH_API_URL = "https://unleash.nais.io/api/";
    @Bean
    @Profile({DEV_FSS, PROD_FSS})
    public Unleash initializeUnleash( ByEnvironmentStrategy byEnvironmentStrategy,
                                      IsNotProdStrategy isNotProdStrategy)
            {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(UNLEASH_API_URL)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy,
                isNotProdStrategy
        );
    }

    @Profile(LOCAL)
    @Bean
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enableAll();
        return fakeUnleash;
    }
}
