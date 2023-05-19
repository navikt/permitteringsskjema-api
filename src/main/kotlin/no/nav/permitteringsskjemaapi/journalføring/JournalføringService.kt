package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.NorgClient.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JournalføringService(
    val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    val journalføringRepository: JournalføringRepository,
    val eregClient: EregClient,
    val norgClient: NorgClient,
    val dokgenClient: DokgenClient,
) {

    val log = logger()

    fun startJournalføring(skjemaid: UUID) {
        log.info("startJournalføring skjemaid=$skjemaid")
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }

    @Transactional // TODO endre til en tx per work item, men schedule hent flere utenfor tx
    @Scheduled(
        initialDelayString = "PT1M",
        //fixedRateString = "PT5S",
        fixedRateString = "PT5M",
    )
    fun processingLoop() {
        val journalføring = journalføringRepository.findWork().getOrNull() ?: return

        // SM
        when (journalføring.state) {
            Journalføring.State.NY -> journalfør(journalføring)
            Journalføring.State.JOURNALFORT -> opprettoppgave(journalføring)
            Journalføring.State.FERDIG -> log.error("uventet state i workitem {}", journalføring)
        }
    }


    private fun journalfør(journalføring: Journalføring) {
        val skjema = permitteringsskjemaRepository.findById(journalføring.skjemaid)
            .orElseThrow { RuntimeException("journalføring finner ikke skjema med id ${journalføring.skjemaid}") }

        val kommunenummer = eregClient.hentKommunenummer(skjema.bedriftNr!!)
        log.info("fant kommunenummer {} for bedrift {}", kommunenummer, skjema.bedriftNr)

        val behandlendeEnhet = if (kommunenummer == null)
            OSLO_ARBEIDSLIVSENTER_KODE
         else
             norgClient.hentBehandlendeEnhet(kommunenummer)

        log.info("fant behandlendeEnhet {} for kommunenummer {}", behandlendeEnhet, kommunenummer)

        if (behandlendeEnhet == null) {
            throw RuntimeException("Behandlende enhet ble ikke funnet. Behandling av melding er avbrutt for Bedrift ${skjema.bedriftNr}")
        }

        val dokumentPdfAsBytes = dokgenClient.genererPdf(skjema)
        log.info("Genererte pdf: {} bytes", dokumentPdfAsBytes.size)

        // kall dokarkiv med journalpost og hent id

        // sett tilstand på journalføring
    }

    private fun opprettoppgave(journalføring: Journalføring) {
        // opprett oppgave

        // lagre oppgave id på journaføring
    }
}