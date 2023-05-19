package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
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

    private val log = logger()

    @Transactional
    @Scheduled(
        initialDelayString = "PT1M",
        fixedDelayString = "PT5S",
    )
    fun scheduleFixedRateTask() {
        log.info("send kafka loop")
        permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100)).forEach { queueItem ->
            val skjema = permitteringsskjemaRepository.findById(queueItem.skjemaId).orElseThrow()
            check(skjema.sendtInnTidspunkt == null) {
                "skjema er ikke sendt inn. sendtInnTidspunkt=null. skjemaId = ${skjema.id}"
            }

            log.info("sender til kafka skjemaid=${skjema.id}")
            permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
            permitteringsmeldingKafkaRepository.delete(queueItem)
        }
    }

    fun scheduleSend(skjemaid: UUID) {
        log.info("scheduleSend til kafka skjemaid=$skjemaid")
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaid))
    }
}