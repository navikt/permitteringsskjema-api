package no.nav.permitteringsskjemaapi.permittering

import jakarta.persistence.*
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
import java.time.Instant
import java.time.LocalDate
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(properties = [
    "spring.flyway.cleanDisabled=false",
    "spring.flyway.validateOnMigrate=false"
])
@MockBean(MultiIssuerConfiguration::class)
@ActiveProfiles("test")
@DirtiesContext
class PermitteringsskjemaRepositoryTest {
    @Autowired
    lateinit var permitteringsskjemaRepository: PermitteringsskjemaRepository

    @Autowired
    lateinit var flyway: Flyway

    @Before
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun kanLagreOgLeseAlleFelter() {
        val skjemaid = UUID.randomUUID()
        val initial = Permitteringsskjema(
            antallBerørt = 10,
            avbrutt = true,
            bedriftNavn = "fooo",
            bedriftNr = "1234",
            fritekst = "friiiiii",
            id = skjemaid,
            kontaktEpost = "lol@lol",
            kontaktNavn = "dora",
            kontaktTlf = "43214123",
            opprettetAv = "mikke",
            opprettetTidspunkt = Instant.now(),
            sendtInnTidspunkt = Instant.now(),
            sluttDato = LocalDate.now().plusDays(30),
            startDato = LocalDate.now(),
            type = PermitteringsskjemaType.INNSKRENKNING_I_ARBEIDSTID,
            ukjentSluttDato = false,
            varsletAnsattDato = LocalDate.now().minusDays(7),
            varsletNavDato = LocalDate.now(),
            yrkeskategorier = mutableListOf(Yrkeskategori(
                id = UUID.randomUUID(),
                permitteringsskjema = null,
                konseptId = 42,
                styrk08 = "1",
                label = "foo",
                antall = 3,
            )),
            årsakskode = Årsakskode.ANDRE_ÅRSAKER,
            årsakstekst = "bare fordi",
        )
        permitteringsskjemaRepository.save(initial)
        val readback = permitteringsskjemaRepository.findById(initial.id!!).orElseThrow()

        assertNotNull(readback.antallBerørt)
        assertNotNull(readback.avbrutt)
        assertNotNull(readback.bedriftNavn)
        assertNotNull(readback.bedriftNr)
        assertNotNull(readback.fritekst)
        assertNotNull(readback.id)
        assertNotNull(readback.kontaktEpost)
        assertNotNull(readback.kontaktNavn)
        assertNotNull(readback.kontaktTlf)
        assertNotNull(readback.opprettetAv)
        assertNotNull(readback.opprettetTidspunkt)
        assertNotNull(readback.sendtInnTidspunkt)
        assertNotNull(readback.sluttDato)
        assertNotNull(readback.startDato)
        assertNotNull(readback.type)
        assertNotNull(readback.ukjentSluttDato)
        assertNotNull(readback.varsletAnsattDato)
        assertNotNull(readback.varsletNavDato)
        assertNotNull(readback.yrkeskategorier)
        assertNotNull(readback.årsakskode)
        assertNotNull(readback.årsakstekst)
    }
}