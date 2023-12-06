package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.PermitteringTestData
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
import no.nav.permitteringsskjemaapi.util.TokenUtil
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


@WebMvcTest(
    value = [PermitteringsskjemaController::class],
    properties = ["server.servlet.context-path=/", "tokensupport.enabled=false"]
)
class PermitteringsskjemaControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var tokenUtil: TokenUtil

    @MockBean
    lateinit var altinnService: AltinnService

    @MockBean
    lateinit var repository: PermitteringsskjemaRepository

    @MockBean
    lateinit var journalføringService: JournalføringService

    @MockBean
    lateinit var permitteringsmeldingKafkaService: PermitteringsmeldingKafkaService

    val now = Instant.now()

    @Test
    fun `henter alle skjema sortert`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        `when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf(
            AltinnOrganisasjon(organizationNumber = "1"),
            AltinnOrganisasjon(organizationNumber = "2"),
        ))
        `when`(repository.findAllByBedriftNr("1", fromDate)).thenReturn(listOf(
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "sendt inn 1 minutt siden, opprettet 10 min siden"
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES)
                opprettetTidspunkt = now.minus(10, ChronoUnit.MINUTES)
                opprettetAv = "me"
            }
        ))
        `when`(repository.findAllByBedriftNr("2", fromDate)).thenReturn(listOf(
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "ikke sendt inn opprettet 5 min siden"
                sendtInnTidspunkt = null
                opprettetTidspunkt = now.minus(5, ChronoUnit.MINUTES)
                opprettetAv = "me"
            },
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "sendt inn 1 minutt siden, opprettet 10 min siden"
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES)
                opprettetTidspunkt = now.minus(10, ChronoUnit.MINUTES)
                opprettetAv = "me"
            }
        ))
        `when`(repository.findAllByOpprettetAv("42")).thenReturn(listOf(
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "ikke sendt inn opprettet 10 min siden"
                sendtInnTidspunkt = null
                opprettetTidspunkt = now.minus(10, ChronoUnit.MINUTES)
                opprettetAv = "me"
            },
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "sendt inn 2 minutt siden, opprettet 5 min siden"
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES)
                opprettetTidspunkt = now.minus(5, ChronoUnit.MINUTES)
                opprettetAv = "me"
            }
        ))

        val jsonResponse = mockMvc.get("/skjema") {
            accept(MediaType.APPLICATION_JSON)
        }.andDo {
            MockMvcResultHandlers.print()
        }.andExpect {
            status().isOk
        }.andReturn().response.contentAsString

        val jsonNode : JsonNode = objectMapper.readValue(jsonResponse)
        Assertions.assertThat(
            jsonNode.map { it.at("/bedriftNavn").textValue() }
        ).containsExactly(
            "sendt inn 1 minutt siden, opprettet 10 min siden",
            "sendt inn 1 minutt siden, opprettet 10 min siden",
            "sendt inn 2 minutt siden, opprettet 5 min siden",
            "ikke sendt inn opprettet 5 min siden",
            "ikke sendt inn opprettet 10 min siden",
        )
    }

    @Test
    fun sendInnStarterJournalføringOgSkedulererKafkaSend() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        val skjemaid = skjema.id!!
        `when`(repository.findByIdAndOpprettetAv(skjemaid, "42")).thenReturn(Optional.of(skjema))
        `when`(repository.save(any())).thenReturn(skjema)

        mockMvc.post("/skjema/{id}/send-inn", skjemaid) {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status().isOk
        }

        verify(journalføringService).startJournalføring(skjemaid = skjemaid)
        verify(permitteringsmeldingKafkaService).scheduleSend(skjemaid = skjemaid)
    }

    @Test
    fun sendInnValidererInput() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        val skjemaid = skjema.id!!
        `when`(repository.findByIdAndOpprettetAv(skjemaid, "42")).thenReturn(Optional.of(skjema))
        `when`(repository.save(any())).thenReturn(skjema)

        mockMvc.post("/skjemaV2", skjemaid) {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "type": "MASSEOPPSIGELSE",
                }
                """
        }.andDo {
            MockMvcResultHandlers.print()
        }.andExpect {
            status().is4xxClientError
        }
    }

    @Test
    fun sendInnV2() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        lateinit var savedSkjema : Permitteringsskjema
        `when`(repository.save(any())).then { answer ->
            savedSkjema = answer.getArgument(0)
            savedSkjema
        }

        mockMvc.post("/skjemaV2") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "yrkeskategorier": [
                    {
                      "konseptId": 21837,
                      "label": "Kokkeassistent",
                      "styrk08": "5120.03"
                    }
                  ],
                  "bedriftNr": "910825569",
                  "bedriftNavn": "STORFOSNA OG FREDRIKSTAD REGNSKAP",
                  "ukjentSluttDato": false,
                  "sluttDato": "2023-12-14T23:00:00.000Z",
                  "startDato": "2023-12-09T23:00:00.000Z",
                  "kontaktNavn": "asdf",
                  "kontaktEpost": "ken@g.no",
                  "kontaktTlf": "12341234",
                  "antallBerørt": "12",
                  "årsakskode": "RÅSTOFFMANGEL",
                  "årsakstekst": "Råstoffmangel",
                  "type": "PERMITTERING_UTEN_LØNN",
                  "fritekst": "### Yrker\nKokkeassistent\n### Årsak\nRåstoffmangel",
                  "varsletNavDato": "2023-12-04T13:18:52.715Z",
                  "varsletAnsattDato": "2023-12-04T13:18:52.715Z",
                  "opprettetTidspunkt": "2023-12-04T13:18:52.715Z",
                  "sendtInnTidspunkt": "2023-12-04T13:18:52.715Z"
                }
                """
        }.andDo {
            MockMvcResultHandlers.print()
        }.andExpect {
            status().isOk
        }

        verify(journalføringService).startJournalføring(skjemaid = savedSkjema.id!!)
        verify(permitteringsmeldingKafkaService).scheduleSend(skjemaid = savedSkjema.id!!)
    }
}
