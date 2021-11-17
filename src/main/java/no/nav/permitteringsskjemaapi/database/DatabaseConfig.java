package no.nav.permitteringsskjemaapi.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Profile({"dev-gcp", "prod-gcp"})
@Configuration
public class DatabaseConfig {

    private final String url;

    public DatabaseConfig(@Value("${permittering.database-url}") String url) {
        this.url = url;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setInitializationFailTimeout(60000);

        return new HikariDataSource(config);
    }
}
