package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2Repository
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Service
class PermitteringsmeldingKafkaService(
    private val permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository,
    private val permitteringsskjemaRepository: PermitteringsskjemaRepository,
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
            val skjema = permitteringsskjemaV2Repository.findById(queueItem.skjemaId)?.let { v2 ->
                // TODO: fjern mapping til gammelt domene når v2 er tatt i bruk
                Permitteringsskjema(
                    id =  v2.id,
                    type = v2.type,
                    bedriftNr = v2.bedriftNr,
                    bedriftNavn = v2.bedriftNavn,
                    kontaktNavn = v2.kontaktNavn,
                    kontaktEpost = v2.kontaktEpost,
                    kontaktTlf = v2.kontaktTlf,
                    antallBerørt = v2.antallBerørt,
                    årsakskode = v2.årsakskode,
                    årsakstekst = v2.årsakstekst,
                    yrkeskategorier = v2.yrkeskategorier.map { yk ->
                        Yrkeskategori(
                            label = yk.label,
                            styrk08 = yk.styrk08,
                            konseptId = yk.konseptId,
                        )
                    }.toMutableList(),
                    sendtInnTidspunkt = v2.sendtInnTidspunkt,
                    opprettetAv = v2.opprettetAv,
                    fritekst = v2.fritekst,

                    sluttDato = v2.sluttDato,
                    startDato = v2.startDato,
                    varsletAnsattDato = v2.sendtInnTidspunkt.let { LocalDate.ofInstant(it, ZoneId.systemDefault()) },
                    varsletNavDato = v2.sendtInnTidspunkt.let { LocalDate.ofInstant(it, ZoneId.systemDefault()) },
                )
                // TODO fjern fallback til v1 når v2 er tatt i bruk
            } ?: permitteringsskjemaRepository.findById(queueItem.skjemaId).orElseThrow()
            check(skjema.sendtInnTidspunkt != null) {
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