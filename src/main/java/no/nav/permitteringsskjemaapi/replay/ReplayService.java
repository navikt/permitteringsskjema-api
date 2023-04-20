package no.nav.permitteringsskjemaapi.replay;

import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile({"dev-gcp", "prod-gcp"})
@Service
public class ReplayService {

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

    @Scheduled(fixedRate = 5_000)
    public void scheduleFixedRateTask() {
        replayRepository.fetchReplayQueueItems().forEach(replayQueueItem -> {
            Permitteringsskjema skjema = permitteringsskjemaRepository.findById(replayQueueItem.skjemaId).orElseThrow();
            permitteringsskjemaProdusent.sendInn(new PermitteringsskjemaSendtInn(skjema, "replayer"));
            replayRepository.delete(replayQueueItem);
        });
    }
}
