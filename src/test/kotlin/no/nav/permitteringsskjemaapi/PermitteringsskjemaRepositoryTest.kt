package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.permittering.testSkjema
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.flywaydb.core.Flyway
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*


@RunWith(SpringRunner::class)
@MockBean(MultiIssuerConfiguration::class)
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
        val permitteringsskjema = testSkjema()
        repository.save(permitteringsskjema)
    }

    @Test
    fun skal_kunne_hentes_med_id() {
        val id = UUID.randomUUID()
        val permitteringsskjema = testSkjema(id = id)
        repository.save(permitteringsskjema)
        val hentetPermittering = repository.findById(id)

        assert(hentetPermittering != null)
    }
}