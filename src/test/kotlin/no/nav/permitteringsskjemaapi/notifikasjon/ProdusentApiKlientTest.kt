package no.nav.permitteringsskjemaapi.notifikasjon

import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.fakes.FakeControllerContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = ["server.port=54058"])
@Import(FakeControllerContext::class)
@ActiveProfiles("test")
internal class ProdusentApiKlientTest {
    @Autowired
    lateinit var produsentApiKlient: ProdusentApiKlient

    @Autowired
    lateinit var fakeResponseResolver: FakeControllerContext.FakeResponseResolver

    @MockitoBean
    lateinit var entraIdKlient: EntraIdKlient // stub nais m2m token så den ikke feiler

    @Test
    fun `NySakVellykket fra Produsent api`() = runBlocking {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nySak": {
                    "__typename": "NySakVellykket",
                    "id": "42"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

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

    @Test
    fun `DuplikatGrupperingsid kaster ikke exception`() = runBlocking {
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

    @Test
    fun `NySak Error responser kaster exception`() = runBlocking {
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

    @Test
    fun `NyBeskjedVellykket repsons fra Produsent api`() = runBlocking {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nyBeskjed": {
                    "__typename": "NyBeskjedVellykket",
                    "id": "42"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

        assertDoesNotThrow {
            produsentApiKlient.opprettNyBeskjed(
                grupperingsid = "grupperingsid",
                merkelapp = "merkelapp",
                virksomhetsnummer = "1234",
                tekst = "tittel",
                lenke = "lenke",
                null,
                "eksernid"
            )
        }
    }

    @Test
    fun `DuplikatEksternIdOgMerkelapp kaster ikke exception`() = runBlocking {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nyBeskjed": {
                    "__typename": "DuplikatEksternIdOgMerkelapp",
                    "feilmelding": "Her er en feilmelding"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

        assertDoesNotThrow {
            produsentApiKlient.opprettNyBeskjed(
                grupperingsid = "grupperingsid",
                merkelapp = "merkelapp",
                virksomhetsnummer = "1234",
                tekst = "tittel",
                lenke = "lenke",
                null,
                eksternId = "eksternid"
            )
        }
    }

    @Test
    fun `NyBeskjed Error responser kaster exception`() = runBlocking {
        val ugyldigeResponsTyper = listOf(
            "UgyldigMerkelapp",
            "UgyldigMottaker",
            "UkjentProdusent",
            "EnHeltUkjentResponsType",
        )

        for (responseType in ugyldigeResponsTyper) {
            fakeResponseResolver.setResolver { //language=json
                """{
                    "data": {
                      "nyBeskjed": {
                        "__typename": $responseType,
                        "feilmelding": "Her er en feilmelding"
                        }
                    },
                    "errors": []
                    }
                """.trimIndent()
            }

            assertThrows<Exception> {
                produsentApiKlient.opprettNyBeskjed(
                    grupperingsid = "grupperingsid",
                    merkelapp = "merkelapp",
                    virksomhetsnummer = "1234",
                    tekst = "tittel",
                    lenke = "lenke",
                    null,
                    eksternId = "eksternid"
                )
            }
        }
    }

    @Test
    fun `NyStatusSakVellykket respons fra Produsent api`() = runBlocking {
        fakeResponseResolver.setResolver { //language=json
            """{
                "data": {
                  "nyStatusSakByGrupperingsid": {
                    "__typename": "NyStatusSakVellykket",
                    "id": "42"
                    }
                },
                "errors": []
                }
            """.trimIndent()
        }

        assertDoesNotThrow {
            produsentApiKlient.oppdaterSakStatusTrukket(
                grupperingsid = "grupperingsid",
                merkelapp = "merkelapp",
                overstyrStatustekstMed = null,
            )
        }
    }

    @Test
    fun `NyStatusSak Error responser kaster exception`() = runBlocking {
        val ugyldigeResponsTyper = listOf(
            "SakFinnesIkke",
            "Konflikt",
            "UgyldigMerkelapp",
            "UkjentProdusent",
            "EnHeltUkjentResponsType",
        )

        for (responseType in ugyldigeResponsTyper) {
            fakeResponseResolver.setResolver { //language=json
                """{
                    "data": {
                      "nyStatusSakByGrupperingsid": {
                        "__typename": $responseType,
                        "feilmelding": "Her er en feilmelding"
                        }
                    },
                    "errors": []
                    }
                """.trimIndent()
            }

            assertThrows<Exception> {
                produsentApiKlient.oppdaterSakStatusTrukket(
                    grupperingsid = "grupperingsid",
                    merkelapp = "merkelapp",
                    overstyrStatustekstMed = null,
                )
            }
        }
    }
}
