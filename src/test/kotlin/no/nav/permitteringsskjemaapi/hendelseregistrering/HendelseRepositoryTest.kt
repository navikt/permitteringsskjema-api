package no.nav.permitteringsskjemaapi.hendelseregistrering

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest()
@ActiveProfiles("test")
@DirtiesContext
@MockBean(MultiIssuerConfiguration::class)
class HendelseRepositoryTest {

    @Autowired
    lateinit var repository: HendelseRepository

    @Test
    fun `should save and read hendelse`() {
        val uuid = UUID.randomUUID()
        repository.save(Hendelse.nyHendelse(uuid, HendelseType.OPPRETTET, "42"))

        Assert.assertNotNull(repository.findById(uuid))

    }
}