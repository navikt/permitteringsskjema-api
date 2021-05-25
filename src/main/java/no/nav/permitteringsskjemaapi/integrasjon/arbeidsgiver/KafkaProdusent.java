package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.config.KafkaTemplateFactory;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static no.nav.permitteringsskjemaapi.config.Constants.NAV_CALL_ID;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;

@Service
@ConditionalOnProperty(name = "permittering.arbeidsgiver.enabled")
@Slf4j
public class KafkaProdusent implements Arbeidsgiver {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapperWrapper mapper;
    private final String topic;

    public KafkaProdusent(
            KafkaTemplateFactory kafkaTemplateFactory,
            ObjectMapperWrapper mapper,
            @Value("${kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplateFactory.kafkaTemplate();
        this.mapper = mapper;
        this.topic = topic;
    }

    @EventListener
    public void sendInn(PermitteringsskjemaSendtInn event) {
        sendRapport(event.getPermitteringsskjema());
    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        log.info("Legger permitteringsskjema {} på kø", rapport.getId());
        var record = new ProducerRecord<>(topic, rapport.getId().toString(),
                mapper.writeValueAsString(rapport));
        record.headers().add(new RecordHeader(NAV_CALL_ID, callIdOrNew().getBytes()));
        send(record);
    }

    private void sendRapport(Permitteringsskjema skjema) {
        var rapport = ArbeidsgiverRapport.builder()
                .antallBerorte(skjema.getAntallBerørt())
                .bedriftsnummer(skjema.getBedriftNr())
                .fritekst(skjema.getFritekst())
                .id(skjema.getId())
                .kontaktEpost(skjema.getKontaktEpost())
                .kontaktNavn(skjema.getKontaktNavn())
                .kontaktTlf(skjema.getKontaktTlf())
                .sendtInnTidspunkt(skjema.getSendtInnTidspunkt())
                .sluttDato(skjema.getSluttDato())
                .startDato(skjema.getStartDato())
                .varsletAnsattDato(skjema.getVarsletAnsattDato())
                .varsletNavDato(skjema.getVarsletNavDato())
                .type(skjema.getType())
                .årsakskode(skjema.getÅrsakskode())
                .årsakstekst(skjema.getÅrsakstekst())
                .yrkeskategorier(skjema.getYrkeskategorier())
                .build();
        publiser(rapport);

    }

    private void send(ProducerRecord<String, String> record) {
        log.debug("Sender melding {} på {}", record.value(), topic);
        kafkaTemplate.send(record)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.info("Sendte melding {} med offset {} på {}", record.value(),
                                result.getRecordMetadata().offset(), topic);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        log.warn("Kunne ikke sende melding {} på {}", record.value(), topic, e);
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kafkaOperations=" + kafkaTemplate + ", mapper=" + mapper + ", topic="
                + topic + "]";
    }

}
