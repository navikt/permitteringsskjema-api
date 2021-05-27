package no.nav.permitteringsskjemaapi.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.CommonClientConfigs;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static no.nav.permitteringsskjemaapi.config.Constants.*;

@Profile({DEV_FSS, LOCAL, DEFAULT})
@Component
public class KafkaTemplateFactory {
    private final String keystorePath;
    private final String credstorePassword;
    private final String truststorePath;
    private final String bootstrapServers;

    public KafkaTemplateFactory(
            @Value("${kafka.keystore.path}") String keystorePath,
            @Value("${kafka.credstore.password}") String credstorePassword,
            @Value("${kafka.truststore.path}") String truststorePath,
            @Value("${kafka.brokers}") String bootstrapServers
    ) {
        this.keystorePath = keystorePath;
        this.credstorePassword = credstorePassword;
        this.truststorePath = truststorePath;
        this.bootstrapServers = bootstrapServers;
    }

    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,keystorePath);
        configMap.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,credstorePassword);
        configMap.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,credstorePassword);
        configMap.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,credstorePassword);
        configMap.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,"JKS");
        configMap.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,"PKCS12");
        configMap.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,truststorePath);
        configMap.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,"SSL");
        configMap.put(ProducerConfig.CLIENT_ID_CONFIG,"permitteringsskjema-api");
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        return new KafkaTemplate(new DefaultKafkaProducerFactory(configMap));
    }
}
