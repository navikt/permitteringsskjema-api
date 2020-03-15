package no.nav.permitteringsskjemaapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "permittering")
public class PermitteringConfig {
    private final String topic;
    private final boolean enabled;

    @ConstructorBinding
    public PermitteringConfig(String topic, boolean enabled) {
        this.topic = topic;
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[topic=" + topic + ", enabled=" + enabled + "]";
    }

}
