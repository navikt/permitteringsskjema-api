package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2Repository
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class PermitteringsmeldingKafkaService(
    private val permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository,
    private val permitteringsskjemaV2Repository: PermitteringsskjemaV2Repository,
    private val permitteringsskjemaProdusent: PermitteringsskjemaProdusent
) {

    @Transactional
    @Scheduled(
        initialDelayString = "PT1M",
        fixedDelayString = "PT5S",
    )
    fun scheduleFixedRateTask() {
        permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100)).forEach { queueItem ->
            val skjema = permitteringsskjemaV2Repository.findById(queueItem.skjemaId)
                ?: throw RuntimeException("skjema med id ${queueItem.skjemaId} finnes ikke")

            permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
            permitteringsmeldingKafkaRepository.delete(queueItem)
        }
    }

    fun scheduleSend(skjemaid: UUID) {
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaid))
    }
}