package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.X_CORRELATION_ID
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.Journalføring.State
import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2Repository
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JournalføringService(
    val permitteringsskjemaV2Repository: PermitteringsskjemaV2Repository,
    val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    val journalføringRepository: JournalføringRepository,
    val eregClient: EregClient,
    val norgClient: NorgClient,
    val dokgenClient: DokgenClient,
    val dokarkivClient: DokarkivClient,
    val oppgaveClient: OppgaveClient,
) {

    private val log = logger()

    fun startJournalføring(skjemaid: UUID) {
        log.info("startJournalføring skjemaid=$skjemaid")
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }

    @Transactional
    fun utførJournalføring(): Boolean {
        val journalføring = journalføringRepository.findWork().getOrNull() ?: return false
        try {
            MDC.put(X_CORRELATION_ID, UUID.randomUUID().toString())

            log.info("Plukket ut skjema {} i tilstandsmaskinen for journalføring", journalføring.skjemaid)
            // State machine
            when (journalføring.state) {
                State.NY -> journalfør(journalføring, nesteState = State.JOURNALFORT)
                State.JOURNALFORT -> opprettoppgave(journalføring)
                State.NEEDS_JOURNALFORING_ONLY -> journalfør(journalføring, nesteState = State.FERDIG)
                State.FERDIG -> log.error("uventet state i workitem {}", journalføring)
            }
        } finally {
            MDC.remove(X_CORRELATION_ID)
        }
        return true
    }

    private fun journalfør(journalføring: Journalføring, nesteState: State) {
        val skjema = permitteringsskjemaV2Repository.findById(journalføring.skjemaid)?.let { v2 ->
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
        } ?: permitteringsskjemaRepository.findById(journalføring.skjemaid) // TODO fjern fallback til v1 når v2 er tatt i bruk
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
            .orElseThrow { RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}") }

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
        //fixedRateString = "PT5S",
        fixedRateString = "PT5M",
    )
    fun processingLoop() {
        while (journalføringService.utførJournalføring()) {
            // work happens in while-condition
        }
    }
}
