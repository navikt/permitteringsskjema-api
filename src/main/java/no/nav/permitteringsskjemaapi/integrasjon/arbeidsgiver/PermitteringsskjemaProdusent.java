package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.config.KafkaTemplateFactory;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static no.nav.permitteringsskjemaapi.config.Constants.*;
import static no.nav.permitteringsskjemaapi.config.Constants.DEFAULT;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;

@Service
@Profile({DEV_FSS, LOCAL, DEFAULT})
@Slf4j
public class PermitteringsskjemaProdusent {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapperWrapper mapper;
    private final String topic;
    private final ApplicationContext context;

    public PermitteringsskjemaProdusent(
            KafkaTemplateFactory kafkaTemplateFactory,
            ObjectMapperWrapper mapper,
            @Value("${kafka.topic}") String topic,
            ApplicationContext context) {
        this.kafkaTemplate = kafkaTemplateFactory.kafkaTemplate();
        this.mapper = mapper;
        this.topic = topic;
        this.context = context;
    }

    @EventListener
    public void sendInn(PermitteringsskjemaSendtInn event) {
        sendRapport(event.getPermitteringsskjema());
    }

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
                        log.info("Sendte melding på {}", topic);
                        log.debug("Sendte melding {} med offset {} på {}", record.value(),
                                result.getRecordMetadata().offset(), topic);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        log.error("Kunne ikke sende melding på {}. Dette er kanskje på grunn av rullerte credentials. Appen stoppes.", topic, e);
                        SpringApplication.exit(context);
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kafkaOperations=" + kafkaTemplate + ", mapper=" + mapper + ", topic="
                + topic + "]";
    }

}
