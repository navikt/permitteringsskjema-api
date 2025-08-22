package no.nav.permitteringsskjemaapi.permittering

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class PermitteringsskjemaRepositoryTest {
    @Autowired
    lateinit var permitteringsskjemaRepository: PermitteringsskjemaRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun kanLagreOgLeseAlleFelter() {
        val skjemaid = UUID.randomUUID()
        val initial = Permitteringsskjema(
            antallBerørt = 10,
            bedriftNavn = "fooo",
            bedriftNr = "1234",
            id = skjemaid,
            kontaktEpost = "lol@lol",
            kontaktNavn = "dora",
            kontaktTlf = "43214123",
            opprettetAv = "mikke",
            sendtInnTidspunkt = Instant.now(),
            sluttDato = LocalDate.now().plusDays(30),
            startDato = LocalDate.now(),
            type = SkjemaType.INNSKRENKNING_I_ARBEIDSTID,
            ukjentSluttDato = false,
            yrkeskategorier = listOf(
                Yrkeskategori(
                konseptId = 42,
                styrk08 = "1",
                label = "foo"
            )
            ),
            årsakskode = Årsakskode.ANDRE_ÅRSAKER,
        )
        permitteringsskjemaRepository.save(initial)
        val readback = permitteringsskjemaRepository.findById(initial.id)!!

        assertNotNull(readback.antallBerørt)
        assertNotNull(readback.bedriftNavn)
        assertNotNull(readback.bedriftNr)
        assertNotNull(readback.fritekst)
        assertNotNull(readback.id)
        assertNotNull(readback.kontaktEpost)
        assertNotNull(readback.kontaktNavn)
        assertNotNull(readback.kontaktTlf)
        assertNotNull(readback.opprettetAv)
        assertNotNull(readback.sendtInnTidspunkt)
        assertNotNull(readback.sluttDato)
        assertNotNull(readback.startDato)
        assertNotNull(readback.type)
        assertNotNull(readback.ukjentSluttDato)
        assertNotNull(readback.yrkeskategorier)
        assertNotNull(readback.årsakskode)
        assertNotNull(readback.årsakstekst)
        assertNull(readback.trukketTidspunkt)

        permitteringsskjemaRepository.setTrukketTidspunkt(initial.id, "12345678910")
        val etterTrukketReadback = permitteringsskjemaRepository.findById(initial.id)!!
        assertNotNull(etterTrukketReadback.trukketTidspunkt)
    }
}