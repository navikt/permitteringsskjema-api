package no.nav.permitteringsskjemaapi.notifikasjon

import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.fakes.FakeControllerContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = ["server.port=54058"])
@ExtendWith(SpringExtension::class)
@Import(FakeControllerContext::class)
@ActiveProfiles("test")
internal class ProdusentApiKlientTest {
    @Autowired
    lateinit var produsentApiKlient: ProdusentApiKlient

    @Autowired
    lateinit var fakeResponseResolver: FakeControllerContext.FakeResponseResolver

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

        runBlocking {
            assertDoesNotThrow {
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

    @Test
    fun `Duplikat grupperings kaster ikke exception`() {
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
            assertDoesNotThrow {
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

    @Test
    fun `Ugyldige responser kaster exception`() {
        val ugyldigeResponsTyper = listOf(
            "UgyldigMerkelapp",
            "UgyldigMottaker",
            "UkjentProdusent",
            "UkjentRolle",
            "EnHeltUkjentResponsType",
        )

        for (responseType in ugyldigeResponsTyper) {
            fakeResponseResolver.setResolver { //language=json
                """{
                    "data": {
                      "nySak": {
                        "__typename": $responseType,
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
}

val nySakId: String = UUID.randomUUID().toString()

