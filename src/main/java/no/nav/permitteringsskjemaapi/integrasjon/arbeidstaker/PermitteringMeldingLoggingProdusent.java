package no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidstaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;

@Service
@ConditionalOnMissingBean(PermitteringMeldingKafkaProdusent.class)
public class PermitteringMeldingLoggingProdusent implements Permittering {

    private static final Logger LOG = LoggerFactory.getLogger(PermitteringMeldingLoggingProdusent.class);

    @EventListener
    public void sendInn(SkjemaSendtInn event) {
        LOG.info("Skjema sendt inn id={}", event.getPermitteringsskjema().getId());
        event.getPermitteringsskjema()
                .permittertePersoner().stream()
                .forEach(this::publiser);
    }

    @Override
    public void publiser(PermittertPerson person) {
        LOG.info("Sender {}", person);

    }
}
