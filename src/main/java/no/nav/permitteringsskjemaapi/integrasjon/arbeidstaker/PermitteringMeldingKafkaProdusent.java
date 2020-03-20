package no.nav.permitteringsskjemaapi.integrasjon.arbeidstaker;

import static no.nav.permitteringsskjemaapi.config.Constants.NAV_CALL_ID;
import static no.nav.permitteringsskjemaapi.util.MDCUtil.callIdOrNew;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import no.nav.permitteringsskjemaapi.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import no.nav.permitteringsskjemaapi.integrasjon.Permittering;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.ArbeidsgiverRapport;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.ArbeidsgiverRapportConfig;
import no.nav.permitteringsskjemaapi.util.ObjectMapperWrapper;

@Service
@ConditionalOnProperty(name = "permittering.dagpenger.enabled")
public class PermitteringMeldingKafkaProdusent implements Permittering {

    private static final Logger LOG = LoggerFactory.getLogger(PermitteringMeldingKafkaProdusent.class);

    private final KafkaOperations<String, String> kafkaOperations;
    private final ObjectMapperWrapper mapper;
    private final PermitteringConfig config;
    private final ArbeidsgiverRapportConfig arbeidsgiverConfig;

    public PermitteringMeldingKafkaProdusent(KafkaOperations<String, String> kafkaOperations,
            PermitteringConfig config, ArbeidsgiverRapportConfig arbeidsgiverConfig, ObjectMapperWrapper mapper) {
        this.kafkaOperations = kafkaOperations;
        this.config = config;
        this.arbeidsgiverConfig = arbeidsgiverConfig;
        this.mapper = mapper;
    }

    @EventListener
    public void sendInn(SkjemaSendtInn event) {
        var skjema = event.getPermitteringsskjema();
        var permitterte = skjema.permittertePersoner();
        sendIndividuelle(skjema.getId(), permitterte);
        sendRapport(permitterte.size(), skjema);

    }

    private void sendRapport(int size, Permitteringsskjema skjema) {
        var rapport = ArbeidsgiverRapport.builder()
                .antallBerorte(size)
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
                .type(skjema.getType()).build();
        publiser(rapport);

    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        send(MessageBuilder
                .withPayload(mapper.writeValueAsString(rapport))
                .setHeader(TOPIC, config.getTopic())
                .setHeader(NAV_CALL_ID, callIdOrNew())
                .build());
    }

    private void sendIndividuelle(UUID id, List<PermittertPerson> permitterte) {
        LOG.info("Skjema sendt inn id={}", id);
        permitterte.stream()
                .forEach(this::publiser);

    }

    @Override
    public void publiser(PermittertPerson person) {
        send(MessageBuilder
                .withPayload(mapper.writeValueAsString(person))
                .setHeader(TOPIC, config.getTopic())
                .setHeader(NAV_CALL_ID, callIdOrNew())
                .build());
    }

    private void send(Message<String> message) {
        LOG.info("Sender melding {} på {}", message.getPayload(), config.getTopic());
        kafkaOperations.send(message)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOG.info("Sendte melding {} med offset {} på {}", message.getPayload(),
                                result.getRecordMetadata().offset(), config.getTopic());
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        LOG.warn("Kunne ikke sende melding {} på {}", message.getPayload(), config.getTopic(), e);
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kafkaOperations=" + kafkaOperations + ", mapper=" + mapper + ", config="
                + config + "]";
    }

}
