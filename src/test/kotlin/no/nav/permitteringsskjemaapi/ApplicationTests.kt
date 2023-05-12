package no.nav.permitteringsskjemaapi

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean


@SpringBootTest
@MockBean(MultiIssuerConfiguration::class)
internal class ApplicationTests {
    @Test
    fun contextLoads() {
    }
}