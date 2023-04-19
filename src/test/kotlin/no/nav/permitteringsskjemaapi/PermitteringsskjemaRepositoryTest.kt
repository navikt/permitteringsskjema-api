package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class PermitteringsskjemaRepositoryTest {
    private var lagretPermitteringsskjema: Permitteringsskjema? = null
    private var permitteringsskjema: Permitteringsskjema? = null

    @Autowired
    lateinit var repository: PermitteringsskjemaRepository

    @Before
    fun setUp() {
        permitteringsskjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        lagretPermitteringsskjema = repository.save(permitteringsskjema!!)
    }

    @Test
    fun skal_kunne_lagre_alle_felter() {
        Assertions.assertThat(lagretPermitteringsskjema)
            .usingRecursiveComparison() // Felt-for-felt sammenligning
            .isEqualTo(permitteringsskjema)
    }

    @Test
    fun skal_kunne_hentes_med_id() {
        val hentetPermittering = repository.findById(permitteringsskjema!!.id!!)
        Assertions.assertThat(hentetPermittering).hasValue(permitteringsskjema)
    }
}