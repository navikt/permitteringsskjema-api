package no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidsgiver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "permittering.arbeidsgiver")
public class ArbeidsgiverRapportConfig {
    private final String topic;
    private final boolean enabled;

    @ConstructorBinding
    public ArbeidsgiverRapportConfig(String topic, boolean enabled) {
        this.topic = topic;
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
