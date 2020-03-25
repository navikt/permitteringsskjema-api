package no.nav.permitteringsskjemaapi.integrasjon.arbeidstaker;

import static no.nav.permitteringsskjemaapi.config.Constants.NAV_CALL_ID;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;

import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;

@Service
@ConditionalOnProperty(name = "permittering.dagpenger.enabled")
public class PermitteringMeldingKafkaProdusent implements Permittering {

    private static final Logger LOG = LoggerFactory.getLogger(PermitteringMeldingKafkaProdusent.class);

    private final KafkaOperations<String, String> kafkaOperations;
    private final ObjectMapperWrapper mapper;
    private final PermitteringConfig config;

    public PermitteringMeldingKafkaProdusent(KafkaOperations<String, String> kafkaOperations,
            PermitteringConfig config, ObjectMapperWrapper mapper) {
        this.kafkaOperations = kafkaOperations;
        this.config = config;
        this.mapper = mapper;
    }

    @EventListener
    public void sendInn(SkjemaSendtInn event) {
        var skjema = event.getPermitteringsskjema();
        var permitterte = skjema.permittertePersoner();
        sendIndividuelle(skjema.getId(), permitterte);

    }

    private void sendIndividuelle(UUID id, List<PermittertPerson> permitterte) {
        LOG.info("Skjema sendt inn id={}", id);
        permitterte.stream()
                .forEach(this::publiser);
    }

    @Override
    public void publiser(PermittertPerson person) {
        var record = new ProducerRecord<>(config.getTopic(), person.getPerson().getFnr(),
                mapper.writeValueAsString(person));
        record.headers().add(new RecordHeader(NAV_CALL_ID, callIdOrNew().getBytes()));
        send(record);
    }

    private void send(ProducerRecord<String, String> record) {
        LOG.debug("Sender melding {} på {}", record.value(), config.getTopic());
        kafkaOperations.send(record)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOG.debug("Sendte melding {} med offset {} på {}", record.value(),
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
