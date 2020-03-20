package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import static no.nav.permitteringsskjemaapi.config.Constants.NAV_CALL_ID;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;

@Service
@ConditionalOnProperty(name = "permittering.arbeidsgiver.enabled")
public class ArbeidsgiverMeldingKafkaProdusent implements Arbeidsgiver {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsgiverMeldingKafkaProdusent.class);

    private final KafkaOperations<String, String> kafkaOperations;
    private final ObjectMapperWrapper mapper;
    private final ArbeidsgiverRapportConfig config;

    public ArbeidsgiverMeldingKafkaProdusent(KafkaOperations<String, String> kafkaOperations,
            ArbeidsgiverRapportConfig config, ObjectMapperWrapper mapper) {
        this.kafkaOperations = kafkaOperations;
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        var record = new ProducerRecord<>(config.getTopic(), rapport.getBedriftsnummer(),
                mapper.writeValueAsString(rapport));
        record.headers().add(new RecordHeader(NAV_CALL_ID, callIdOrNew().getBytes()));
        send(record);
    }

    private void send(ProducerRecord<String, String> record) {
        LOG.info("Sender melding {} på {}", record.value(), config.getTopic());
        kafkaOperations.send(record)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOG.info("Sendte melding {} med offset {} på {}", record.value(),
                                result.getRecordMetadata().offset(), config.getTopic());
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        LOG.warn("Kunne ikke sende melding {} på {}", record.value(), config.getTopic(), e);
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kafkaOperations=" + kafkaOperations + ", mapper=" + mapper + ", config="
                + config + "]";
    }

}
