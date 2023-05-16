package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.Resource
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.time.Instant
import java.time.LocalDate

@MockBean(MultiIssuerConfiguration::class)
@RestClientTest(
    components = [DokgenClient::class],
)
class DokgenClientTest {
    @Autowired
    lateinit var dokgenClient: DokgenClient

    @Autowired
    lateinit var server: MockRestServiceServer

    @Value("example.pdf")
    lateinit var pdfExample: Resource

    private val skjema = Permitteringsskjema(
        antallBerørt = 1,
        bedriftNavn = "hey",
        bedriftNr = "hey",
        fritekst = "hey",
        kontaktEpost = "hey",
        kontaktNavn = "hey",
        kontaktTlf = "hey",
        opprettetAv = "hey",
        opprettetTidspunkt = Instant.parse("2010-01-01T01:01:01Z"),
        sendtInnTidspunkt = Instant.parse("2010-01-01T01:01:01Z"),
        sluttDato = LocalDate.parse("2020-01-01"),
        startDato = LocalDate.parse("2020-01-01"),
        type = PermitteringsskjemaType.INNSKRENKNING_I_ARBEIDSTID,
    )

    @Test
    fun pdfResultIsAccepted() {
        server.expect(requestTo("/template/permittering/create-pdf"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(
                pdfExample.contentAsByteArray,
                org.springframework.http.MediaType.APPLICATION_PDF
            ))

        val bytes = dokgenClient.genererPdf(skjema)
        assertTrue(bytes.size > 4)
    }

    @Test
    fun htmlResultIsRejected() {
        server.expect(requestTo("/template/permittering/create-pdf"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(
                "<html foob ar",
                org.springframework.http.MediaType.APPLICATION_PDF
            ))

        assertThrows(Throwable::class.java) {
            dokgenClient.genererPdf(skjema)
        }
    }
}