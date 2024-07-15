package no.nav.permitteringsskjemaapi.journalføring

import jakarta.transaction.Transactional
import no.nav.permitteringsskjemaapi.journalføring.Journalføring.State
import no.nav.permitteringsskjemaapi.journalføring.Journalføring.State.*
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import no.nav.permitteringsskjemaapi.timeline
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import java.time.Instant
import java.time.LocalDate
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.hours

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class JournalføringServiceTest {
    companion object {
        const val `Orgnr Sarpsborg vask og rens AS` = "111111111"
        const val `Sarpsborgs kommunenummer` = "0102"
        const val `Behandlende enhet i Sarpsborg` = "222"
        const val `Journalpost-id Sarpsborg vask og rens AS` = "333"
        const val `Oppgave-id Sarpsbort vask og rens AS` = "444"

        // Det er ikke alle foretak som er knyttet til kommunenummer.
        const val `Orgnr Stockholm NUF` = "222222222"
        const val `Journalpost-id Stockholm NUF` = "555"
        const val `Oppgave-id Stockholm NUF` = "1532"

        val eregReturnVirksomhetNotFound = AtomicBoolean(false)

        fun withVirksomhetNotFound(block: () -> Unit) {
            try {
                eregReturnVirksomhetNotFound.set(true)
                block()
            } finally {
                eregReturnVirksomhetNotFound.set(false)
            }
        }


        val eregClient = EregClient {
            if (eregReturnVirksomhetNotFound.get())
                throw VirksomhetNotFoundException()
            else
                when (it) {
                    `Orgnr Sarpsborg vask og rens AS` -> `Sarpsborgs kommunenummer`
                    `Orgnr Stockholm NUF` -> null
                    else -> throw RuntimeException()
                }}
    }

    @TestConfiguration
    class Config {
        @Primary @Bean fun eregClient() = eregClient

        @Primary
        @Bean
        fun norgClient() = NorgClient { when (it) {
            `Sarpsborgs kommunenummer` -> `Behandlende enhet i Sarpsborg`
            else -> throw RuntimeException()
        }}

        @Primary
        @Bean
        fun dokgenClient() = DokgenClient { _ -> "pdf".toByteArray() }

        @Primary
        @Bean
        fun dokarkivClient() = DokarkivClient { skjema, _enhet, _pdf -> when (skjema.bedriftNr) {
            `Orgnr Sarpsborg vask og rens AS` -> `Journalpost-id Sarpsborg vask og rens AS`
            `Orgnr Stockholm NUF` -> `Journalpost-id Stockholm NUF`
            else -> throw RuntimeException()
        }}

        @Primary
        @Bean
        fun oppgaveClient() = OppgaveClient { _skjema, journalføring -> when (journalføring.journalpostId) {
            `Journalpost-id Sarpsborg vask og rens AS` -> `Oppgave-id Sarpsbort vask og rens AS`
            `Journalpost-id Stockholm NUF` -> `Oppgave-id Stockholm NUF`
            else -> throw RuntimeException()
        }}
    }

    @Autowired
    lateinit var permitteringsskjemaRepository: PermitteringsskjemaRepository

    @Autowired
    lateinit var journalføringRepository: JournalføringRepository

    @Autowired
    lateinit var journalføringService: JournalføringService

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    @Transactional
    fun `happy path med vanlig virksomhet`() {
        val id = nyttSkjema(bedriftNr = `Orgnr Sarpsborg vask og rens AS`)

        transaction {
            assertTrue(journalføringService.utførJournalføring())
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(JOURNALFORT, journalføring.state)
            assertEquals(`Sarpsborgs kommunenummer`, journalføring.journalført!!.kommunenummer)
            assertEquals(`Behandlende enhet i Sarpsborg`, journalføring.journalført!!.behandlendeEnhet)
            assertEquals(`Journalpost-id Sarpsborg vask og rens AS`, journalføring.journalført!!.journalpostId)
        }

        transaction {
            assertTrue(journalføringService.utførJournalføring())
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(FERDIG, journalføring.state)
            assertEquals(`Oppgave-id Sarpsbort vask og rens AS`, journalføring.oppgave!!.oppgaveId)
        }
    }

    @Test
    @Transactional
    fun `happy path med vanlig virksomhet men ereg er ikke oppdatert`() {
        val (t0, t1) = timeline(step = 1.hours)
        val id = nyttSkjema(bedriftNr = `Orgnr Sarpsborg vask og rens AS`)

        transaction {
            withVirksomhetNotFound {
                assertTrue(journalføringService.utførJournalføring(now = t0))
            }
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(NY, journalføring.state)
            assertNull(journalføring.journalført)
        }

        transaction {
            assertTrue(journalføringService.utførJournalføring(now = t1))
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(JOURNALFORT, journalføring.state)
            assertEquals(`Sarpsborgs kommunenummer`, journalføring.journalført!!.kommunenummer)
            assertEquals(`Behandlende enhet i Sarpsborg`, journalføring.journalført!!.behandlendeEnhet)
            assertEquals(`Journalpost-id Sarpsborg vask og rens AS`, journalføring.journalført!!.journalpostId)
        }

        transaction {
            assertTrue(journalføringService.utførJournalføring(now = t1))
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(FERDIG, journalføring.state)
            assertEquals(`Oppgave-id Sarpsbort vask og rens AS`, journalføring.oppgave!!.oppgaveId)
        }
    }

    @Test
    @Transactional
    fun `happy path for virksomhet uten kommunenummer`() {
        val id = nyttSkjema(bedriftNr = `Orgnr Stockholm NUF`)

        transaction {
            assertTrue(journalføringService.utførJournalføring())
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(JOURNALFORT, journalføring.state)
            assertEquals(null, journalføring.journalført!!.kommunenummer)
            assertEquals(NorgClient.OSLO_ARBEIDSLIVSENTER_KODE, journalføring.journalført!!.behandlendeEnhet)
            assertEquals(`Journalpost-id Stockholm NUF`, journalføring.journalført!!.journalpostId)
        }

        transaction {
            assertTrue(journalføringService.utførJournalføring())
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(FERDIG, journalføring.state)
            assertEquals(`Oppgave-id Stockholm NUF`, journalføring.oppgave!!.oppgaveId)
        }
    }

    @Test
    @Transactional
    fun `happy path med vanlig virksomhet og kun journalføring`() {
        val id = nyttSkjema(bedriftNr = `Orgnr Sarpsborg vask og rens AS`, state = NEEDS_JOURNALFORING_ONLY)

        transaction {
            assertTrue(journalføringService.utførJournalføring())
        }

        transaction {
            val journalføring = journalføringRepository.findById(id).get()
            assertEquals(FERDIG, journalføring.state)
            assertEquals(`Sarpsborgs kommunenummer`, journalføring.journalført!!.kommunenummer)
            assertEquals(`Behandlende enhet i Sarpsborg`, journalføring.journalført!!.behandlendeEnhet)
            assertEquals(`Journalpost-id Sarpsborg vask og rens AS`, journalføring.journalført!!.journalpostId)
            assertNull(journalføring.oppgave)
        }
    }

    private fun nyttSkjema(bedriftNr: String, state: State = NY) =
        UUID.randomUUID().also { id ->
            transaction {
                permitteringsskjemaRepository.save(
                    Permitteringsskjema(
                        id = id,
                        type = SkjemaType.INNSKRENKNING_I_ARBEIDSTID,

                        bedriftNr = bedriftNr,
                        bedriftNavn = "Foo AS",

                        kontaktNavn = "Ola N",
                        kontaktEpost = "test@nav.no",
                        kontaktTlf = "0".repeat(0),

                        antallBerørt = 4,
                        årsakskode = Årsakskode.BRANN,

                        yrkeskategorier = listOf(),
                        startDato = LocalDate.parse("2020-01-01"),
                        sluttDato = null,
                        ukjentSluttDato = true,

                        sendtInnTidspunkt = Instant.now(),
                        opprettetAv = "X",
                    )
                )
                Journalføring(skjemaid = id).also {
                    it.state = state
                    journalføringRepository.save(it)
                }
            }
        }
}

private fun <T: Any?> transaction(block: () -> T): T {
    if (!TestTransaction.isActive()) TestTransaction.start()
    val result = block()
    TestTransaction.flagForCommit()
    TestTransaction.end()
    return result
}
