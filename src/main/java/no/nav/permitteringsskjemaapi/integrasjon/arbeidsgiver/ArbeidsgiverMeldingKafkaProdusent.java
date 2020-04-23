package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static no.nav.permitteringsskjemaapi.config.Constants.NAV_CALL_ID;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;

@Service
@ConditionalOnProperty(name = "permittering.arbeidsgiver.enabled")
@Slf4j
@RequiredArgsConstructor
public class ArbeidsgiverMeldingKafkaProdusent implements Arbeidsgiver {
    private final KafkaOperations<String, String> kafkaOperations;
    private final ObjectMapperWrapper mapper;
    private final ArbeidsgiverRapportConfig config;

    @EventListener
    public void sendInn(PermitteringsskjemaSendtInn event) {
        sendRapport(event.getPermitteringsskjema());
    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        log.info("Legger permitteringsskjema {} på kø", rapport.getId());
        var record = new ProducerRecord<>(config.getTopic(), rapport.getId().toString(),
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
        log.debug("Sender melding {} på {}", record.value(), config.getTopic());
        kafkaOperations.send(record)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.debug("Sendte melding {} med offset {} på {}", record.value(),
                                result.getRecordMetadata().offset(), config.getTopic());
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        log.warn("Kunne ikke sende melding {} på {}", record.value(), config.getTopic(), e);
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kafkaOperations=" + kafkaOperations + ", mapper=" + mapper + ", config="
                + config + "]";
    }

}
