package no.nav.permitteringsskjemaapi.replay

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Profile("dev-gcp", "prod-gcp")
@Service
class ReplayService(
    private val replayRepository: ReplayRepository,
    private val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    private val permitteringsskjemaProdusent: PermitteringsskjemaProdusent
) {
    private val log = logger()

    @Transactional
    @Scheduled(fixedRate = 5000)
    fun scheduleFixedRateTask() {
        replayRepository.fetchReplayQueueItems(Pageable.ofSize(10)).forEach { replayQueueItem ->
            val skjema = permitteringsskjemaRepository.findById(replayQueueItem.skjemaId!!).orElseThrow()!!
            if (skjema.sendtInnTidspunkt == null) {
                log.warn("skjema er ikke sendt inn, skipper replay av skjemaId {}", skjema.id)
            } else {
                permitteringsskjemaProdusent.sendInn(skjema)
                replayRepository.delete(replayQueueItem)
            }
        }
    }
}