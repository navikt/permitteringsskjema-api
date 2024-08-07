package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.core.io.Resource
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.time.Instant
import java.time.LocalDate
import java.util.*

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
        id = UUID.randomUUID(),
        antallBerørt = 1,
        bedriftNavn = "hey",
        bedriftNr = "hey",
        kontaktEpost = "hey",
        kontaktNavn = "hey",
        kontaktTlf = "hey",
        opprettetAv = "hey",
        sendtInnTidspunkt = Instant.parse("2010-01-01T01:01:01Z"),
        startDato = LocalDate.parse("2020-01-01"),
        sluttDato = LocalDate.parse("2020-01-01"),
        ukjentSluttDato = false,
        type = SkjemaType.INNSKRENKNING_I_ARBEIDSTID,
        yrkeskategorier = listOf(Yrkeskategori(1, "hey", "hey")),
        årsakskode = Årsakskode.MANGEL_PÅ_ARBEID,
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