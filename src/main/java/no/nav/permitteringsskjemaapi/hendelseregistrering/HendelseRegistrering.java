package no.nav.permitteringsskjemaapi.hendelseregistrering;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaAvbrutt;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaEndret;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaOpprettet;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@AllArgsConstructor
public class HendelseRegistrering {
    private final HendelseRepository repository;

    @EventListener
    public void opprettet(SkjemaOpprettet event) {
        UUID skjemaId = event.getPermitteringsskjema().getId();
        Hendelse hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.OPPRETTET, event.getUtførtAv()));
        log.info("Skjema opprettet skjemaId={} hendelseId={}", skjemaId.toString(), hendelse.getId().toString());
    }

    @EventListener
    public void endret(SkjemaEndret event) {
        UUID skjemaId = event.getPermitteringsskjema().getId();
        Hendelse hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.ENDRET, event.getUtførtAv()));
        log.info("Skjema endret skjemaId={} hendelseId={}", skjemaId.toString(), hendelse.getId().toString());
    }

    @EventListener
    public void sendtInn(SkjemaSendtInn event) {
        UUID skjemaId = event.getPermitteringsskjema().getId();
        Hendelse hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.SENDT_INN, event.getUtførtAv()));
        log.info("Skjema sendt inn skjemaId={} hendelseId={}", skjemaId.toString(), hendelse.getId().toString());
    }

    @EventListener
    public void avbrutt(SkjemaAvbrutt event) {
        UUID skjemaId = event.getPermitteringsskjema().getId();
        Hendelse hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.AVBRUTT, event.getUtførtAv()));
        log.info("Skjema avbrutt skjemaId={} hendelseId={}", skjemaId.toString(), hendelse.getId().toString());
    }
}
