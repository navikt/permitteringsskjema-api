package no.nav.permitteringsskjemaapi.permittering

import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2Repository
import no.nav.permitteringsskjemaapi.permittering.v2.YrkeskategoriV2
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.flywaydb.core.Flyway
import org.junit.Assert.assertNotNull
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
    lateinit var permitteringsskjemaRepository: PermitteringsskjemaV2Repository

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
        val initial = PermitteringsskjemaV2(
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
            type = PermitteringsskjemaType.INNSKRENKNING_I_ARBEIDSTID,
            ukjentSluttDato = false,
            yrkeskategorier = listOf(YrkeskategoriV2(
                konseptId = 42,
                styrk08 = "1",
                label = "foo"
            )),
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
    }
}