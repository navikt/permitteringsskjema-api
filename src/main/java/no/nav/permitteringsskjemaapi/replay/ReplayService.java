package no.nav.permitteringsskjemaapi.replay;

import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.invoke.MethodHandles;

@Profile({"dev-gcp", "prod-gcp"})
@Service
public class ReplayService {

    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ReplayRepository replayRepository;
    private final PermitteringsskjemaRepository permitteringsskjemaRepository;
    private final PermitteringsskjemaProdusent permitteringsskjemaProdusent;

    public ReplayService(
            ReplayRepository replayRepository,
            PermitteringsskjemaRepository permitteringsskjemaRepository,
            PermitteringsskjemaProdusent permitteringsskjemaProdusent
    ) {
        this.replayRepository = replayRepository;
        this.permitteringsskjemaRepository = permitteringsskjemaRepository;
        this.permitteringsskjemaProdusent = permitteringsskjemaProdusent;
    }

    @Transactional
    @Scheduled(fixedRate = 5_000)
    public void scheduleFixedRateTask() {
        replayRepository.fetchReplayQueueItems(Pageable.ofSize(10)).forEach(replayQueueItem -> {
            Permitteringsskjema skjema = permitteringsskjemaRepository.findById(replayQueueItem.skjemaId).orElseThrow();
            if (skjema.getSendtInnTidspunkt() == null) {
                log.warn("skjema er ikke sendt inn, skipper replay av skjemaId {}", skjema.getId());
            } else {
                permitteringsskjemaProdusent.sendInn(new PermitteringsskjemaSendtInn(skjema, "replayer"));
                replayRepository.delete(replayQueueItem);
            }
        });
    }
}
