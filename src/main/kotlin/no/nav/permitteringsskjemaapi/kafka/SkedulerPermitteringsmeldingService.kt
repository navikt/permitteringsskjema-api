package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.notifikasjon.ProdusentApiKlient
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.util.urlTilPermitteringsløsningFrontend
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class SkedulerPermitteringsmeldingService(
    private val permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository,
    private val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    private val permitteringsskjemaProdusent: PermitteringsskjemaProdusent,
    private val produsentApiKlient: ProdusentApiKlient
) {

    @Transactional
    @Scheduled(
        initialDelayString = "PT1M",
        fixedDelayString = "PT5S",
    )
    fun scheduleFixedRateTask() {
        permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100)).forEach { queueItem ->
            val skjema = permitteringsskjemaRepository.findById(queueItem.skjemaId)
                ?: throw RuntimeException("skjema med id ${queueItem.skjemaId} finnes ikke")

            runBlocking {
                produsentApiKlient.opprettNySak(
                    grupperingsid = skjema.id.toString(),
                    tittel = skjema.type.tittel,
                    merkelapp = skjema.type.merkelapp,
                    virksomhetsnummer = skjema.bedriftNr,
                    lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                )
            }

            permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
            permitteringsmeldingKafkaRepository.delete(queueItem)
        }
    }

    fun scheduleSend(skjemaid: UUID) {
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaid))
    }
}