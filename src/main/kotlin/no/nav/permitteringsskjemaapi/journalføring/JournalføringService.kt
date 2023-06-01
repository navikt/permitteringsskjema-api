package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.X_CORRELATION_ID
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.slf4j.MDC
import org.springframework.context.annotation.Profile


@Service
class JournalføringService(
    val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    val journalføringRepository: JournalføringRepository,
    val eregClient: EregClient,
    val norgClient: NorgClient,
    val dokgenClient: DokgenClient,
    val dokarkivClient: DokarkivClient,
    val oppgaveClient: OppgaveClient,
) {

    val log = logger()

    fun startJournalføring(skjemaid: UUID) {
        log.info("startJournalføring skjemaid=$skjemaid")
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }


    @Transactional
    fun utførJournalføring(): Boolean {
        val journalføring = journalføringRepository.findWork().getOrNull() ?: return false
        try {
            MDC.put(X_CORRELATION_ID, UUID.randomUUID().toString())
            // State machine
            when (journalføring.state) {
                Journalføring.State.NY -> journalfør(journalføring)
                Journalføring.State.JOURNALFORT -> opprettoppgave(journalføring)
                Journalføring.State.FERDIG -> log.error("uventet state i workitem {}", journalføring)
            }
        } finally {
            MDC.remove(X_CORRELATION_ID)
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

        log.info(
            "fant behandlendeEnhet {} for kommunenummer {} for skjema {}",
            behandlendeEnhet,
            kommunenummer,
            skjema.id
        )

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
        val skjema = permitteringsskjemaRepository.findById(journalføring.skjemaid)
            .orElseThrow { RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}") }

        val journalført = journalføring.journalført
            ?: throw RuntimeException("Skjema ${journalføring.skjemaid} må være journalført før oppgave kan opprettes")

        val oppgaveId = oppgaveClient.lagOppgave(skjema, journalført)

        journalføring.oppgave = Oppgave(
            oppgaveId = oppgaveId,
            oppgaveOpprettetAt = Instant.now().toString(),
        )

        journalføring.state = Journalføring.State.FERDIG
        journalføringRepository.save(journalføring)
        log.info("Opprettet oppgave {} for skjema {}", oppgaveId, skjema.id)
    }
}

@Component
@Profile("!prod-gcp")
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
