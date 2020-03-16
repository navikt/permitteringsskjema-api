package no.nav.permitteringsskjemaapi.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "permittering.database")
public class DatabaseProperties {
    private String databaseNavn;
    private String databaseUrl;
    private String vaultSti;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private Integer maxLifetime;
}
