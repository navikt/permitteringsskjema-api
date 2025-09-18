package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.notifikasjon.ProdusentApiKlient
import no.nav.permitteringsskjemaapi.permittering.HendelseType
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.util.urlTilPermitteringsløsningFrontend
import org.springframework.dao.DataIntegrityViolationException
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
            runBlocking {
                val skjema = permitteringsskjemaRepository.findById(queueItem.skjemaId)
                    ?: throw RuntimeException("skjema med id ${queueItem.skjemaId} finnes ikke")

                when (queueItem.hendelseType) {
                    HendelseType.TRUKKET -> {
                        permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
                        permitteringsmeldingKafkaRepository.delete(queueItem)
                        produsentApiKlient.opprettNyBeskjed(
                            tekst = skjema.type.trukketTekst,
                            grupperingsid = skjema.id.toString(),
                            merkelapp = skjema.type.merkelapp,
                            virksomhetsnummer = skjema.bedriftNr,
                            lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                            tidspunkt = (skjema.trukketTidspunkt).toString(),
                            eksternId = "${skjema.id}-trukket",
                        )
                        return@runBlocking
                    }

                    HendelseType.INNSENDT -> {
                        produsentApiKlient.opprettNySak(
                            tittel = skjema.type.tittel,
                            grupperingsid = skjema.id.toString(),
                            merkelapp = skjema.type.merkelapp,
                            virksomhetsnummer = skjema.bedriftNr,
                            lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                            tidspunkt = skjema.sendtInnTidspunkt.toString()
                        )
                        produsentApiKlient.opprettNyBeskjed(
                            tekst = skjema.type.beskjedTekst,
                            grupperingsid = skjema.id.toString(),
                            merkelapp = skjema.type.merkelapp,
                            virksomhetsnummer = skjema.bedriftNr,
                            lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                            tidspunkt = skjema.sendtInnTidspunkt.toString(),
                            eksternId = skjema.id.toString(),
                        )

                        permitteringsskjemaProdusent.sendTilKafkaTopic(skjema)
                        permitteringsmeldingKafkaRepository.delete(queueItem)
                    }
                }
            }
        }
    }

    fun scheduleSendInnsendt(skjemaId: UUID) {
        if (permitteringsmeldingKafkaRepository.existsBySkjemaIdAndHendelseType(skjemaId, HendelseType.INNSENDT)) return
        try {
            permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaId, HendelseType.INNSENDT))
        } catch (_: DataIntegrityViolationException) {
            // unique constraint race på (skjemaId, INNSENDT)
        }
    }


    fun scheduleSendTrukket(skjemaId: UUID) {
        if (permitteringsmeldingKafkaRepository.existsBySkjemaIdAndHendelseType(skjemaId, HendelseType.TRUKKET)) return
        try {
            permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(skjemaId, HendelseType.TRUKKET))
        } catch (_: DataIntegrityViolationException) {
            // unique constraint race på (skjemaId, TRUKKET)
        }
    }
}
