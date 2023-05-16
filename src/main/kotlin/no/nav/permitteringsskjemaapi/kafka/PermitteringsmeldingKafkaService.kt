package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PermitteringsmeldingKafkaService(
    private val permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository,
    private val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    private val permitteringsskjemaProdusent: PermitteringsskjemaProdusent
) {
    private val log = logger()

    @Transactional
    @Scheduled(fixedRate = 5000)
    fun scheduleFixedRateTask() {
        permitteringsmeldingKafkaRepository.fetchReplayQueueItems(Pageable.ofSize(10)).forEach { replayQueueItem ->
            val skjema = permitteringsskjemaRepository.findById(replayQueueItem.skjemaId!!).orElseThrow()!!
            if (skjema.sendtInnTidspunkt == null) {
                log.warn("skjema er ikke sendt inn, skipper replay av skjemaId {}", skjema.id)
            } else {
                permitteringsskjemaProdusent.sendInn(skjema)
                permitteringsmeldingKafkaRepository.delete(replayQueueItem)
            }
        }
    }

    fun scheduleSend(permitteringsskjema: Permitteringsskjema?) {
        TODO("Not yet implemented")
    }
}