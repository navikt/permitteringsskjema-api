package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JournalføringService(
    val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    val journalføringRepository: JournalføringRepository,
    val eregClient: EregClient,
    val norgClient: NorgClient,
    val dokgenClient: DokgenClient,
    val dokarkivClient: DokarkivClient,
) {

    val log = logger()

    fun startJournalføring(skjemaid: UUID) {
        log.info("startJournalføring skjemaid=$skjemaid")
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }


    @Transactional
    fun utførJournalføring(): Boolean {
        val journalføring = journalføringRepository.findWork().getOrNull() ?: return false
        // SM
        when (journalføring.state) {
            Journalføring.State.NY -> journalfør(journalføring)
            Journalføring.State.JOURNALFORT -> opprettoppgave(journalføring)
            Journalføring.State.FERDIG -> log.error("uventet state i workitem {}", journalføring)
        }
        return true
    }

    private fun journalfør(journalføring: Journalføring) {
        val skjema = permitteringsskjemaRepository.findById(journalføring.skjemaid)
            .orElseThrow { RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}") }

        val kommunenummer = eregClient.hentKommunenummer(skjema.bedriftNr!!)
        log.info("fant kommunenummer {} for skjema {}", kommunenummer, skjema.id)

        val behandlendeEnhet = if (kommunenummer == null)
            OSLO_ARBEIDSLIVSENTER_KODE
         else
             norgClient.hentBehandlendeEnhet(kommunenummer)

        log.info("fant behandlendeEnhet {} for kommunenummer {} for skjema {}", behandlendeEnhet, kommunenummer, skjema.id)

        if (behandlendeEnhet == null) {
            throw RuntimeException("Behandlende enhet ble ikke funnet. Behandling av melding er avbrutt for Bedrift ${skjema.bedriftNr} for skjema ${skjema.id}")
        }

        val dokumentPdfAsBytes = dokgenClient.genererPdf(skjema)
        log.info("Genererte pdf ({} bytes) for skjema {}", dokumentPdfAsBytes.size, skjema.id)

        // kall dokarkiv med journalpost og hent id
        val journalpostid: String = dokarkivClient.opprettjournalPost(
            skjema = skjema,
            behandlendeEnhet = behandlendeEnhet,
            dokumentPdfAsBytes = dokumentPdfAsBytes,
        )
        log.info("Opprettet journalpost med id {} for skjema {}", journalpostid, skjema.id)

        journalføring.journalført = Journalført(
            journalpostId = journalpostid,
            journalfortAt = Instant.now().toString(),
            kommunenummer = kommunenummer,
            behandlendeEnhet = behandlendeEnhet,
        )
        journalføring.state = Journalføring.State.JOURNALFORT

        journalføringRepository.save(journalføring)
    }

    private fun opprettoppgave(journalføring: Journalføring) {
        // TODO
        log.info("skulle ha opprettet oppgave for {}", journalføring.skjemaid)

        journalføring.state = Journalføring.State.FERDIG
        journalføringRepository.save(journalføring)
    }
}

@Component
class JournalføringScheduledWorker(
    private val journalføringService: JournalføringService,
) {
    /** Flyttet metoden ut av `JournalføringService`, siden `utførJournalføring()`
     * er markert med @Transactional, og trenger å gå gjennom proxy-objektet.
     */
    @Scheduled(
        initialDelayString = "PT1M",
        //fixedRateString = "PT5S",
        fixedRateString = "PT5M",
    )
    fun processingLoop() {
        while (journalføringService.utførJournalføring()) {
            // work happens in while-condition
        }
    }
}
