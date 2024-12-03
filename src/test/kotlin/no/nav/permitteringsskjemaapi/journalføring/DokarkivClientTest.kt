package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.util.*


private const val dokarkivScope = "api://localhost/.default"

@RestClientTest(
    components = [
        DokarkivClient::class,
        EntraIdKlient::class,
    ],
    properties = [
        "dokarkiv.scope=$dokarkivScope",
    ]
)
class DokarkivClientTest {
    @Autowired
    lateinit var dokarkivClient: DokarkivClient

    @MockBean
    lateinit var entraIdKlient: EntraIdKlient

    @Autowired
    lateinit var server: MockRestServiceServer

    val mockAzureToken = "lol42"
    val behandlendeEnhet = "OSL42"
    val pdfContent = "hello pdf"
    val skjema = Permitteringsskjema(
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
    fun oppretterJournalpostMedAzureToken() {
        Mockito.`when`(entraIdKlient.getToken(dokarkivScope)).thenReturn(mockAzureToken)

        server.expect(requestTo("/journalpost?forsoekFerdigstill=true"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(AUTHORIZATION, "Bearer $mockAzureToken"))
            .andExpect(jsonPath("$.datoMottatt").value(LocalDate.ofInstant(skjema.sendtInnTidspunkt!!, systemDefault()).toString()))
            .andExpect(jsonPath("$.bruker.id").value(skjema.bedriftNr!!))
            .andExpect(jsonPath("$.bruker.idType").value("ORGNR"))
            .andExpect(jsonPath("$.avsenderMottaker.id").value(skjema.bedriftNr!!))
            .andExpect(jsonPath("$.avsenderMottaker.navn").value(skjema.bedriftNavn!!))
            .andExpect(jsonPath("$.avsenderMottaker.idType").value("ORGNR"))
            .andExpect(jsonPath("$.eksternReferanseId").value("PRM-${skjema.id}"))
            .andExpect(jsonPath("$.journalfoerendeEnhet").value(behandlendeEnhet))
            .andExpect(jsonPath("$.dokumenter[0].brevkode").value("NAV 76-08.03"))
            .andExpect(jsonPath("$.dokumenter[0].tittel").value("Arbeidsgivers meldeplikt til NAV ved masseoppsigelser, permitteringer uten lønn og innskrenking i arbeidstiden"))
            .andExpect(jsonPath("$.dokumenter[0].dokumentVarianter[0].filtype").value("PDFA"))
            .andExpect(jsonPath("$.dokumenter[0].dokumentVarianter[0].variantformat").value("ARKIV"))
            .andExpect(jsonPath("$.dokumenter[0].dokumentVarianter[0].fysiskDokument").value(Base64.getEncoder().encodeToString(pdfContent.toByteArray())))
            // TODO assert content?
            .andRespond(
                withSuccess(
                    journalpostOpprettetResponse,
                    MediaType.APPLICATION_JSON
                )
            )

        val journalpostid = dokarkivClient.opprettjournalPost(skjema, behandlendeEnhet, pdfContent.toByteArray())
        assertEquals("42", journalpostid)
    }
}

private val journalpostOpprettetResponse = """
    {
      "dokumenter": [
        {
          "dokumentInfoId": "123"
        }
      ],
      "journalpostId": "42",
      "journalpostferdigstilt": true
    }
""".trimIndent()