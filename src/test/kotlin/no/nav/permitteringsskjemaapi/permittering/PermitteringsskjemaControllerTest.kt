package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.PermitteringTestData
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.hendelseregistrering.HendelseRegistrering
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.util.TokenUtil
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@MockBean(
    PermitteringsskjemaProdusent::class,
    HendelseRegistrering::class,
)
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

    val now = Instant.now()

    @Test
    fun `henter alle skjema sortert`() {
        Mockito.`when`(tokenUtil.autentisertBruker()).thenReturn("42")
        Mockito.`when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf(
            AltinnOrganisasjon(organizationNumber = "1"),
            AltinnOrganisasjon(organizationNumber = "2"),
        ))
        Mockito.`when`(repository.findAllByBedriftNr("1")).thenReturn(listOf(
            PermitteringTestData.enPermitteringMedAltFyltUt().apply {
                bedriftNavn = "sendt inn 1 minutt siden, opprettet 10 min siden"
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES)
                opprettetTidspunkt = now.minus(10, ChronoUnit.MINUTES)
                opprettetAv = "me"
            }
        ))
        Mockito.`when`(repository.findAllByBedriftNr("2")).thenReturn(listOf(
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
        Mockito.`when`(repository.findAllByOpprettetAv("42")).thenReturn(listOf(
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

        val jsonResponse = mockMvc
            .perform(MockMvcRequestBuilders.get("/skjema").accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

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
    fun sendInnStarterJournalføring() {
        Mockito.`when`(tokenUtil.autentisertBruker()).thenReturn("42")
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        val skjemaid = skjema.id!!
        Mockito.`when`(repository.findByIdAndOpprettetAv(skjemaid, "42")).thenReturn(Optional.of(skjema))
        Mockito.`when`(repository.save(any())).thenReturn(skjema)

        mockMvc
            .perform(post("/skjema/{id}/send-inn", skjemaid).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(journalføringService).startJournalføring(skjemaid = skjemaid)
    }
}