package no.nav.permitteringsskjemaapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan("no.nav.permitteringsskjemaapi")
@Profile("test")
class LokalApplikasjon : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(Application::class.java)
                .main(LokalApplikasjon::class.java)
                .run(*args)
        }
    }
}