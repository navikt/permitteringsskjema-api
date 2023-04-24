package no.nav.permitteringsskjemaapi.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class DatabaseConfig{

    @Profile("dev-gcp", "prod-gcp")
    @Bean
    fun dataSource(
        @Value("\${permittering.database.url}") url: String,
        @Value("\${permittering.database.username}") username: String,
        @Value("\${permittering.database.password}") password: String
    ): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = url
        config.username = username
        config.password = password
        config.maximumPoolSize = 2
        config.minimumIdle = 1
        config.initializationFailTimeout = 60000
        return HikariDataSource(config)
    }

    @Bean
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager().apply {
        this.entityManagerFactory = entityManagerFactory
    }
}