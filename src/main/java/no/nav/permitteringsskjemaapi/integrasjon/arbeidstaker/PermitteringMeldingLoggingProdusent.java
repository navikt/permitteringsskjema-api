package no.nav.permitteringsskjemaapi.integrasjon.arbeidstaker;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.permittering.PermittertPerson;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(PermitteringMeldingKafkaProdusent.class)
@Slf4j
public class PermitteringMeldingLoggingProdusent implements Permittering {

    @EventListener
    public void sendInn(PermitteringsskjemaSendtInn event) {
        log.info("Skjema sendt inn id={}", event.getPermitteringsskjema().getId());
        event.getPermitteringsskjema()
                .permittertePersoner().stream()
                .forEach(this::publiser);
    }

    @Override
    public void publiser(PermittertPerson person) {
        log.debug("Sender {}", person);
    }

}
