package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.assertj.core.api.Assertions
import org.flywaydb.core.Flyway
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES


@RunWith(SpringRunner::class)
@SpringBootTest(properties = ["spring.flyway.cleanDisabled=false"])
@ActiveProfiles("test")
class PermitteringsskjemaRepositoryTest {


    @Autowired
    lateinit var repository: PermitteringsskjemaRepository

    @Autowired
    lateinit var flyway: Flyway

    @Before
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun skal_kunne_lagre_alle_felter() {
        val permitteringsskjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        val lagretPermitteringsskjema = repository.save(permitteringsskjema)

        Assertions.assertThat(lagretPermitteringsskjema)
            .usingRecursiveComparison() // Felt-for-felt sammenligning
            .isEqualTo(permitteringsskjema)
    }

    @Test
    fun skal_kunne_hentes_med_id() {
        val permitteringsskjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        repository.save(permitteringsskjema)
        val hentetPermittering = repository.findById(permitteringsskjema.id!!)
        Assertions.assertThat(hentetPermittering).hasValue(permitteringsskjema)
    }

    @Test
    fun `skal hente skjema sortert på sendtinn eller opprettet tidspunkt`() {
        val now = Instant.now()
        PermitteringTestData.enPermitteringMedAltFyltUt().apply {
            bedriftNavn = "sendt inn 1 minutt siden, opprettet 10 min siden"
            sendtInnTidspunkt = now.minus(1, MINUTES)
            opprettetTidspunkt = now.minus(10, MINUTES)
            opprettetAv = "me"
        }.also { repository.save(it) }
        PermitteringTestData.enPermitteringMedAltFyltUt().apply {
            bedriftNavn = "ikke sendt inn opprettet 5 min siden"
            sendtInnTidspunkt = null
            opprettetTidspunkt = now.minus(5, MINUTES)
            opprettetAv = "me"
        }.also { repository.save(it) }
        PermitteringTestData.enPermitteringMedAltFyltUt().apply {
            bedriftNavn = "ikke sendt inn opprettet 10 min siden"
            sendtInnTidspunkt = null
            opprettetTidspunkt = now.minus(10, MINUTES)
            opprettetAv = "me"
        }.also { repository.save(it) }
        PermitteringTestData.enPermitteringMedAltFyltUt().apply {
            bedriftNavn = "sendt inn 2 minutt siden, opprettet 5 min siden"
            sendtInnTidspunkt = now.minus(2, MINUTES)
            opprettetTidspunkt = now.minus(5, MINUTES)
            opprettetAv = "me"
        }.also { repository.save(it) }

        Assertions.assertThat(
            repository.findAllByOpprettetAv("me").map { it.bedriftNavn!! }
        ).containsExactly(
            "sendt inn 1 minutt siden, opprettet 10 min siden",
            "sendt inn 2 minutt siden, opprettet 5 min siden",
            "ikke sendt inn opprettet 5 min siden",
            "ikke sendt inn opprettet 10 min siden",
        )
    }
}