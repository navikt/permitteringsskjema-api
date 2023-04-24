package no.nav.permitteringsskjemaapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener

@Profile("dev-gcp")
@Configuration
class DevKafkaListenerConfig {
    private val log = logger()

    @KafkaListener(
        id = "dev-logging-consumer",
        topics = ["permittering-og-nedbemanning.aapen-permittering-arbeidsgiver"],
        containerFactory = "errorLoggingKafkaListenerContainerFactory"
    )
    fun devLoggingConsumer(message: String?) {
        log.info(
            "Leste melding på topic {}, melding: {}",
            "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver",
            message
        )
    }

}