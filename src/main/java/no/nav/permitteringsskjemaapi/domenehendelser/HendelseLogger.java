package no.nav.permitteringsskjemaapi.domenehendelser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HendelseLogger {
    @EventListener
    public void opprettet(SkjemaOpprettet event) {
        log.info("Skjema opprettet id={}", event.getPermitteringsskjema().getId().toString());
    }

    @EventListener
    public void endret(SkjemaEndret event) {
        log.info("Skjema endret id={}", event.getPermitteringsskjema().getId().toString());
    }
}
