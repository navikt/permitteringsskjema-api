package no.nav.permitteringsskjemaapi.notifikasjon

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.api.Unprotected
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = ["server.port=54058"])
@ExtendWith(SpringExtension::class)
@Import(TestContext::class)
@ActiveProfiles("test")
internal class ProdusentApiKlientImplTest {
    @Autowired
    lateinit var produsentApiKlient: ProdusentApiKlient

    @Autowired
    lateinit var fakeResponseResolver: TestContext.FakeResponseResolver

    @Test
    fun `Gyldig repsons fra Produsent api`() {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nySak": {
                    "__typename": "NySakVellykket",
                    "id": "$nySakId"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

        val response = runBlocking {
            produsentApiKlient.opprettNySak(
                grupperingsid = "grupperingsid",
                merkelapp = "merkelapp",
                virksomhetsnummer = "1234",
                tittel = "tittel",
                lenke = "lenke",
                null
            )
        }
        assert(response == nySakId)
    }

    @Test
    fun `Duplikat grupperings id`() {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nySak": {
                    "__typename": "DuplikatGrupperingsid",
                    "feilmelding": "Her er en feilmelding"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

        runBlocking {
            assertThrows<Exception> {
                produsentApiKlient.opprettNySak(
                    grupperingsid = "grupperingsid",
                    merkelapp = "merkelapp",
                    virksomhetsnummer = "1234",
                    tittel = "tittel",
                    lenke = "lenke",
                    null
                )
            }
        }
    }
}

val nySakId: String = UUID.randomUUID().toString()

@TestConfiguration
class TestContext {

    @RestController
    @Unprotected //TODO: is this correct?
    @Profile("test")
    class FakeController(private val config: FakeResponseResolver) {

        @PostMapping("/*")
        fun opprettNySak(request: HttpServletRequest): String {
            return config.resolveResponse()
        }
    }

    @Component
    class FakeResponseResolver {
        private var resolver: () -> String = { throw Exception("No resolver set") }

        fun setResolver(resolver: () -> String) {
            this.resolver = resolver
        }

        fun resolveResponse(): String {
            return this.resolver.invoke()
        }
    }
}
