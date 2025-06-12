package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import no.nav.permitteringsskjemaapi.permittering.testSkjema
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*


@SpringBootTest
@ActiveProfiles("test")
class PermitteringsskjemaRepositoryTest {
    @Autowired
    lateinit var repository: PermitteringsskjemaRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun skal_kunne_lagre_alle_felter() {
        val permitteringsskjema = testSkjema(bedriftNr = "999999")
        repository.save(permitteringsskjema)
    }

    @Test
    fun skal_kunne_hentes_med_id() {
        val id = UUID.randomUUID()
        val permitteringsskjema = testSkjema(id = id, bedriftNr = "999999")
        repository.save(permitteringsskjema)
        val hentetPermittering = repository.findById(id)

        assert(hentetPermittering != null)
    }
}