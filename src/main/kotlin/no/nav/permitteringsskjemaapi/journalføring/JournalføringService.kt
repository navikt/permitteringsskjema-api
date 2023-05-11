package no.nav.permitteringsskjemaapi.journalføring

import org.springframework.stereotype.Service
import java.util.*

@Service
class JournalføringService(val journalføringRepository: JournalføringRepository) {

    fun startJournalføring(skjemaid: UUID) {
        journalføringRepository.save(Journalføring(skjemaid = skjemaid))
    }
}