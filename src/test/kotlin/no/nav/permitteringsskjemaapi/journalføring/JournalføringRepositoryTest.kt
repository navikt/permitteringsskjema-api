package no.nav.permitteringsskjemaapi.journalføring

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.flywaydb.core.Flyway
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(properties = [
    "spring.flyway.cleanDisabled=false",
    "spring.flyway.validateOnMigrate=false"
])
@MockBean(MultiIssuerConfiguration::class)
@ActiveProfiles("test")
@DirtiesContext
class JournalføringRepositoryTest {
    @Autowired
    lateinit var journalføringRepository: JournalføringRepository

    @Autowired
    lateinit var flyway: Flyway

    @Before
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
        assertEquals(Journalføring.State.NY, nyStateReadBack.state)
        assertNull(nyStateReadBack.journalført)

        /* lagre og lese: Oppdatere til journalført-state */

        nyStateReadBack.journalført = Journalført(
            journalpostId = "someId",
            journalfortAt = "someTime",
            kommunenummer = "1234",
            behandlendeEnhet = "4321",
        )
        nyStateReadBack.state = Journalføring.State.JOURNALFORT
        journalføringRepository.save(nyStateReadBack)

        val journalførtStateReadBack = journalføringRepository.findById(eksempelId).get()

        assertNotNull(journalførtStateReadBack.journalført)
        assertEquals(Journalføring.State.JOURNALFORT, journalførtStateReadBack.state)
        assertEquals("someId", journalførtStateReadBack.journalført?.journalpostId)
        assertEquals("someTime", journalførtStateReadBack.journalført?.journalfortAt)
        assertEquals("1234", journalførtStateReadBack.journalført?.kommunenummer)
        assertEquals("4321", journalførtStateReadBack.journalført?.behandlendeEnhet)

        /* lagre og lese: Oppdatere til ferdig-state */
        journalførtStateReadBack.oppgave = Oppgave(
            oppgaveId = "1122",
            oppgaveOpprettetAt = "1234-01-01",
        )
        journalførtStateReadBack.state = Journalføring.State.FERDIG

        journalføringRepository.save(journalførtStateReadBack)
        val ferdigStateReadback = journalføringRepository.findById(eksempelId).get()
        assertEquals(Journalføring.State.FERDIG, ferdigStateReadback.state)

        assertNotNull(ferdigStateReadback.journalført)
        assertEquals("someId", ferdigStateReadback.journalført?.journalpostId)
        assertEquals("someTime", ferdigStateReadback.journalført?.journalfortAt)
        assertEquals("1234", ferdigStateReadback.journalført?.kommunenummer)
        assertEquals("4321", ferdigStateReadback.journalført?.behandlendeEnhet)

        assertNotNull(ferdigStateReadback.oppgave)
        assertEquals("1122", ferdigStateReadback.oppgave?.oppgaveId)
        assertEquals("1234-01-01", ferdigStateReadback.oppgave?.oppgaveOpprettetAt)
    }
}