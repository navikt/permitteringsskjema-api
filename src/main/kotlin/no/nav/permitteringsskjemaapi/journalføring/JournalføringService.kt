package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.NorgService.Companion.OSLO_ARBEIDSLIVSENTER_KODE
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JournalføringService(
    val permitteringsskjemaRepository: PermitteringsskjemaRepository,
    val journalføringRepository: JournalføringRepository,
    val eregService: EregService,
    val norgService: NorgService,
) {

    val log = logger()

    fun startJournalføring(skjemaid: UUID) {
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }

    @Transactional // TODO endre til en tx per work item, men schedule hent flere utenfor tx
    @Scheduled(
    //    fixedRateString = "PT5S"
        fixedRateString = "PT5M"
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

        val kommunenummer = eregService.hentKommunenummer(skjema.bedriftNr)
        log.info("fant kommunenummer {} for bedrift {}", kommunenummer, skjema.bedriftNr)

        val behandlendeEnhet = if (kommunenummer == null)
            OSLO_ARBEIDSLIVSENTER_KODE
         else
             norgService.hentBehandlendeEnhet(kommunenummer)

        log.info("fant behandlendeEnhet {} for kommunenummer {}", behandlendeEnhet, kommunenummer)

        if (behandlendeEnhet == null) {
            throw RuntimeException("Behandlende enhet ble ikke funnet. Behandling av melding er avbrutt for Bedrift ${skjema.bedriftNr}")
        }

        // lag pdf

        // kall dokarkiv med journalpost og hent id

        // sett tilstand på journalføring
    }

    private fun opprettoppgave(journalføring: Journalføring) {
        // opprett oppgave

        // lagre oppgave id på journaføring
    }
}