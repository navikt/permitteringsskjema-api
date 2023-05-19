package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class PermitteringsmeldingKafkaService(
    private val permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository,
    private val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    private val permitteringsskjemaProdusent: PermitteringsskjemaProdusent
) {

    @Transactional
    @Scheduled(
        initialDelayString = "PT1M",
        fixedDelayString = "PT5S",
    )
    fun scheduleFixedRateTask() {
        permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100)).forEach { queueItem ->
            val skjema = permitteringsskjemaRepository.findById(queueItem.skjemaId).orElseThrow()
            check(skjema.sendtInnTidspunkt == null) {
                "skjema er ikke sendt inn. sendtInnTidspunkt=null. skjemaId = ${skjema.id}"
            }

            permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
            permitteringsmeldingKafkaRepository.delete(queueItem)
        }
    }

    fun scheduleSend(skjemaid: UUID) {
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaid))
    }
}