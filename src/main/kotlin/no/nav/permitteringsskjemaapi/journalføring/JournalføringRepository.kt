package no.nav.permitteringsskjemaapi.journalføring

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface JournalføringRepository : JpaRepository<Journalføring, UUID>