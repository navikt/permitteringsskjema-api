package no.nav.permitteringsskjemaapi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

import static no.nav.permitteringsskjemaapi.config.Constants.*;

@Configuration
@Profile({DEV_FSS})
@Slf4j
public class DevKafkaConsumerConfig {

    private final String keystorePath;
    private final String credstorePassword;
    private final String truststorePath;
    private final String bootstrapServers;
    private final String securityProtocol;

    public DevKafkaConsumerConfig(
            @Value("${kafka.keystore.path}") String keystorePath,
            @Value("${kafka.credstore.password}") String credstorePassword,
            @Value("${kafka.truststore.path}") String truststorePath,
            @Value("${kafka.brokers}") String bootstrapServers,
            @Value("${kafka.security.protocol}") String securityProtocol
    ) {
        this.keystorePath = keystorePath;
        this.credstorePassword = credstorePassword;
        this.truststorePath = truststorePath;
        this.bootstrapServers = bootstrapServers;
        this.securityProtocol = securityProtocol;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
        configMap.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword);
        configMap.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword);
        configMap.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credstorePassword);
        configMap.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
        configMap.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        configMap.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
        configMap.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        configMap.put(ConsumerConfig.CLIENT_ID_CONFIG, "permitteringsskjema-api");
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 20000);
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(1);
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @KafkaListener(topics = "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver", groupId = "dev-logging-consumer")
    public void listenGroupFoo(String message) {
        log.info("Leste melding på topic {}, melding: {}", "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver", message);
    }
}
