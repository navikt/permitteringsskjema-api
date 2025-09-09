package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.X_CORRELATION_ID
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.Journalføring.State
import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.HendelseType
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MINUTES
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
    val oppgaveClient: OppgaveClient,
) {

    private val log = logger()

    fun startJournalføring(skjemaid: UUID, hendelseType: HendelseType = HendelseType.INNSENDT) {
        log.info("startJournalføring skjemaid=$skjemaid, hendelseType=$hendelseType")
        journalføringRepository.save(Journalføring(skjemaid = skjemaid, hendelseType = hendelseType))
    }

    @Transactional
    fun utførJournalføring(now: Instant = Instant.now()): Boolean {
        val journalføring = journalføringRepository.findWork(now).getOrNull() ?: return false
        try {
            MDC.put(X_CORRELATION_ID, UUID.randomUUID().toString())

            log.info("Plukket ut skjema i tilstandsmaskinen for journalføring. {}", journalføring)
            // State machine
            when (journalføring.state) {
                State.NY -> journalfør(journalføring, nesteState = State.JOURNALFORT)
                State.JOURNALFORT -> opprettoppgave(journalføring)
                State.NEEDS_JOURNALFORING_ONLY -> journalfør(journalføring, nesteState = State.FERDIG)
                State.FERDIG -> log.error("uventet state i workitem {}", journalføring)
            }
        } catch (e: VirksomhetNotFoundException) {
            log.info("Virksomhet ikke funnet i EREG: prøver igjen senere.", e)
            journalføring.delayedUntil = now.plus(1, HOURS)
            journalføringRepository.save(journalføring)
        } catch (e: Exception) {
            log.error("Exception ved journalføring: {}", e.message, e)
            journalføring.delayedUntil = now.plus(1, MINUTES)
            journalføringRepository.save(journalføring)
        } finally {
            MDC.remove(X_CORRELATION_ID)
        }
        return true
    }

    private fun journalfør(journalføring: Journalføring, nesteState: State) {
        val skjema = permitteringsskjemaRepository.findById(journalføring.skjemaid)
            ?: throw RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}")

        val kommunenummer = eregClient.hentKommunenummer(skjema.bedriftNr)
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

        val dokumentPdfAsBytes = when (journalføring.hendelseType) {
            HendelseType.TRUKKET -> dokgenClient.genererTrukketPdf(skjema)
            HendelseType.INNSENDT -> dokgenClient.genererPdf(skjema)
        }

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
        journalføring.state = nesteState

        journalføringRepository.save(journalføring)
    }

    private fun opprettoppgave(journalføring: Journalføring) {
        if (journalføring.oppgave != null) {
            log.info("Oppretter ikke oppgave for skjema {}, da det ser ut til å allerede eksistere", journalføring.skjemaid)
            journalføring.state = State.FERDIG
            journalføringRepository.save(journalføring)
            return
        }


        val skjema = permitteringsskjemaRepository.findById(journalføring.skjemaid)
            ?: throw RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}")

        val journalført = journalføring.journalført
            ?: throw RuntimeException("Skjema ${journalføring.skjemaid} må være journalført før oppgave kan opprettes")

        val oppgaveId = oppgaveClient.lagOppgave(skjema, journalført)

        journalføring.oppgave = Oppgave(
            oppgaveId = oppgaveId,
            oppgaveOpprettetAt = Instant.now().toString(),
        )

        journalføring.state = State.FERDIG
        journalføringRepository.save(journalføring)
        log.info("Opprettet oppgave {} for skjema {}", oppgaveId, skjema.id)
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
        fixedDelayString = "PT10S",
    )
    fun processingLoop() {
        while (journalføringService.utførJournalføring()) {
            // work happens in while-condition
        }
    }
}
