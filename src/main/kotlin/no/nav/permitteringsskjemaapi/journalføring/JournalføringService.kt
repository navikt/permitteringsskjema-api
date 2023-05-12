package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.config.logger
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

    @Transactional
    @Scheduled(fixedRateString = "PT5S")
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

        // hent fra ereg (navn. kommunenummer)
        val organisasjon = eregService.hentUnderenhet(skjema.bedriftNr)
            ?: throw RuntimeException("Fant ")

        // hent fra norg (behandlingsenhet eller oslo hvis null)

        // lag pdf

        // kall dokarkiv med journalpost og hent id

        // sett tilstand på journalføring
    }

    private fun opprettoppgave(journalføring: Journalføring) {
        // opprett oppgave

        // lagre oppgave id på journaføring
    }
}