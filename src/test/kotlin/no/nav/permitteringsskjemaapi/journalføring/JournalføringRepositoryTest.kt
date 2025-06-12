package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.journalføring.Journalføring.State.*
import no.nav.permitteringsskjemaapi.timeline
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class JournalføringRepositoryTest {
    @Autowired
    lateinit var journalføringRepository: JournalføringRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun initialStateLagreLese() {
        /* Lagre og lese start-state */
        val eksempelId = UUID.randomUUID()!!
        val nyState = Journalføring(
            skjemaid = eksempelId
        )

        journalføringRepository.save(nyState)
        val nyStateReadBack = journalføringRepository.findById(eksempelId).get()

        assertEquals(eksempelId, nyStateReadBack.skjemaid)
        assertEquals(NY, nyStateReadBack.state)
        assertNull(nyStateReadBack.journalført)

        /* lagre og lese: Oppdatere til journalført-state */

        nyStateReadBack.journalført = Journalført(
            journalpostId = "someId",
            journalfortAt = "someTime",
            kommunenummer = "1234",
            behandlendeEnhet = "4321",
        )
        nyStateReadBack.state = JOURNALFORT
        journalføringRepository.save(nyStateReadBack)

        val journalførtStateReadBack = journalføringRepository.findById(eksempelId).get()

        assertNotNull(journalførtStateReadBack.journalført)
        assertEquals(JOURNALFORT, journalførtStateReadBack.state)
        assertEquals("someId", journalførtStateReadBack.journalført?.journalpostId)
        assertEquals("someTime", journalførtStateReadBack.journalført?.journalfortAt)
        assertEquals("1234", journalførtStateReadBack.journalført?.kommunenummer)
        assertEquals("4321", journalførtStateReadBack.journalført?.behandlendeEnhet)

        /* lagre og lese: Oppdatere til ferdig-state */
        journalførtStateReadBack.oppgave = Oppgave(
            oppgaveId = "1122",
            oppgaveOpprettetAt = "1234-01-01",
        )
        journalførtStateReadBack.state = FERDIG

        journalføringRepository.save(journalførtStateReadBack)
        val ferdigStateReadback = journalføringRepository.findById(eksempelId).get()
        assertEquals(FERDIG, ferdigStateReadback.state)

        assertNotNull(ferdigStateReadback.journalført)
        assertEquals("someId", ferdigStateReadback.journalført?.journalpostId)
        assertEquals("someTime", ferdigStateReadback.journalført?.journalfortAt)
        assertEquals("1234", ferdigStateReadback.journalført?.kommunenummer)
        assertEquals("4321", ferdigStateReadback.journalført?.behandlendeEnhet)

        assertNotNull(ferdigStateReadback.oppgave)
        assertEquals("1122", ferdigStateReadback.oppgave?.oppgaveId)
        assertEquals("1234-01-01", ferdigStateReadback.oppgave?.oppgaveOpprettetAt)
    }

    @Test
    @Transactional
    fun findWorkOnEmptyDatabase() {
        val work = journalføringRepository.findWork()
        assertTrue(work.isEmpty)
    }

    @Test
    @Transactional
    fun `NY skjema needs more work`() {
        val skjema = journalføringRepository.nyJournalføring(NY)
        val work = journalføringRepository.findWork()
        assertTrue(work.isPresent)
        assertEquals(skjema.skjemaid, work.get().skjemaid)
    }

    @Test
    @Transactional
    fun `JOURNALFORT skjema needs more work`() {
        val skjema = journalføringRepository.nyJournalføring(JOURNALFORT)
        val work = journalføringRepository.findWork()
        assertTrue(work.isPresent)
        assertEquals(skjema.skjemaid, work.get().skjemaid)
    }

    @Test
    @Transactional
    fun `FERDIG skjema is finished`() {
        journalføringRepository.nyJournalføring(FERDIG)
        val work = journalføringRepository.findWork()
        assertTrue(work.isEmpty)
    }

    @Test
    @Transactional
    fun `NEEDS_JOURNALFORING_ONLY skjema needs more work`() {
        val skjema = journalføringRepository.nyJournalføring(NEEDS_JOURNALFORING_ONLY)
        val work = journalføringRepository.findWork()
        assertTrue(work.isPresent)
        assertEquals(skjema.skjemaid, work.get().skjemaid)
    }

    @Test
    @Transactional
    fun `work in order of arrival`() {
        val førsteSkjema = journalføringRepository.nyJournalføring(NY)
        val andreSkjema = journalføringRepository.nyJournalføring(NY)
        assertTrue(førsteSkjema.rowInsertedAt < andreSkjema.rowInsertedAt) /* precondition */

        val work = journalføringRepository.findWork()
        assertTrue(work.isPresent)
        assertEquals(førsteSkjema.skjemaid, work.get().skjemaid)
    }


    @Test
    @Transactional
    fun `prioritize work which has not been delayed`() {
        val (t0, t1) = timeline()
        val førsteSkjema = journalføringRepository.nyJournalføring(NY)
        val andreSkjema = journalføringRepository.nyJournalføring(NY)

        førsteSkjema.delayedUntil = t1
        journalføringRepository.save(førsteSkjema)

        val work = journalføringRepository.findWork(now = t0)
        assertTrue(work.isPresent)
        assertEquals(andreSkjema.skjemaid, work.get().skjemaid)
    }

    @Test
    @Transactional
    fun `dont find work before delay`() {
        val (t0, t1) = timeline()
        journalføringRepository.nyJournalføring(NY, delayUntil = t1)

        val work = journalføringRepository.findWork(now = t0)
        assertTrue(work.isEmpty)
    }

    @Test
    @Transactional
    fun `find work after delay`() {
        val (t0, t1) = timeline()
        val skjema = journalføringRepository.nyJournalføring(NY, delayUntil = t0)

        val work = journalføringRepository.findWork(now = t1)
        assertTrue(work.isPresent)
        assertEquals(skjema.skjemaid, work.get().skjemaid)
    }

    @Test
    @Transactional
    fun `treg og feilende journalføring fører ikke til starvation`() {
        val (t0, t1, t2, t3) = timeline()

        /* Denne testen simulerer noe ganske usannsynlig: at prosesseringen går så tregt
         * at en utsatt prosessering blir aktuell igjen før alle de andre skjemaene har
         * hatt en mulighet for å ha fremdrift. */

        val førsteSkjema = journalføringRepository.nyJournalføring(NY, delayUntil = t0)
        val andreSkjema = journalføringRepository.nyJournalføring(NY, delayUntil = t0)
        assertTrue(førsteSkjema.rowInsertedAt < andreSkjema.rowInsertedAt) /* precondition */

        journalføringRepository.findWork(now = t1).also {
            assertTrue(it.isPresent)
            val skjema = it.get()
            assertEquals(førsteSkjema.skjemaid, skjema.skjemaid)
            skjema.delayedUntil = t2
            journalføringRepository.save(skjema)
        }

        journalføringRepository.findWork(now = t3).also {
            assertTrue(it.isPresent)
            val skjema = it.get()
            assertEquals(andreSkjema.skjemaid, skjema.skjemaid)
        }
    }

    companion object {
        private fun JournalføringRepository.nyJournalføring(
            state: Journalføring.State,
            delayUntil: Instant? = null,
        ): Journalføring {
            val id = UUID.randomUUID()
            return Journalføring(skjemaid = id).also {
                it.state = state
                it.delayedUntil = delayUntil
                save(it)
            }
        }
    }
}