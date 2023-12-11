package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2
import no.nav.permitteringsskjemaapi.permittering.v2.PermitteringsskjemaV2Repository
import no.nav.permitteringsskjemaapi.permittering.v2.YrkeskategoriV2
import no.nav.permitteringsskjemaapi.util.TokenUtil
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@MockBean(TokenValidationContextHolder::class)
@SpringBootTest(
    properties = [
        "tokensupport.enabled=false",
        "server.servlet.context-path=/",
        "spring.flyway.cleanDisabled=false",
    ]
)
@AutoConfigureMockMvc
class PermitteringsskjemaIntegrationTest {

    @MockBean
    lateinit var altinnService: AltinnService

    @MockBean
    lateinit var tokenUtil: TokenUtil


    @MockBean
    lateinit var journalføringService: JournalføringService

    @MockBean
    lateinit var permitteringsmeldingKafkaService: PermitteringsmeldingKafkaService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repository: PermitteringsskjemaV2Repository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
    }

    val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @Test
    fun `GET skjemaV2 henter alle skjema sortert`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        `when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf(
            AltinnOrganisasjon(organizationNumber = "1"),
            AltinnOrganisasjon(organizationNumber = "2"),
        ))
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 1 min siden",
                bedriftNr = "1",
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES),
                opprettetAv = "Noen Andrè",
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 5 min siden",
                bedriftNr = "2",
                sendtInnTidspunkt = now.minus(5, ChronoUnit.MINUTES),
                opprettetAv = "Noen Andrè",
            ),
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 10 min siden",
                sendtInnTidspunkt = now.minus(10, ChronoUnit.MINUTES),
                opprettetAv = "42",
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 2 min siden",
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                opprettetAv = "42",
            )
        )

        val jsonResponse = mockMvc.get("/skjemaV2") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status().isOk
        }.andReturn().response.contentAsString

        val jsonNode : JsonNode = objectMapper.readValue(jsonResponse)
        Assertions.assertThat(
            jsonNode.map { it.at("/bedriftNavn").textValue() }
        ).containsExactly(
            "sendt inn 1 min siden",
            "sendt inn 2 min siden",
            "sendt inn 5 min siden",
            "sendt inn 10 min siden",
        )
    }

    @Test
    fun `GET skjemaV2 by id henter skjema lagret av bruker`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        `when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf())

        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                opprettetAv = "42",
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status().isOk
            content {
                json(
                    """
                    {
                      "id": "${lagretSkjema.id}",
                      "type": "${lagretSkjema.type}",
                      "bedriftNr": "${lagretSkjema.bedriftNr}",
                      "bedriftNavn": "${lagretSkjema.bedriftNavn}",
                      "kontaktNavn": "${lagretSkjema.kontaktNavn}",
                      "kontaktEpost": "${lagretSkjema.kontaktEpost}",
                      "kontaktTlf": "${lagretSkjema.kontaktTlf}",
                      "antallBerørt": ${lagretSkjema.antallBerørt},
                      "årsakskode": "${lagretSkjema.årsakskode}",
                      "årsakstekst": "${lagretSkjema.årsakskode.navn}",
                      "yrkeskategorier": [
                        {
                          "konseptId": ${lagretSkjema.yrkeskategorier.first().konseptId},
                          "label": "${lagretSkjema.yrkeskategorier.first().label}",
                          "styrk08": "${lagretSkjema.yrkeskategorier.first().styrk08}"
                        }
                      ],
                      "ukjentSluttDato": ${lagretSkjema.ukjentSluttDato},
                      "sluttDato": "${lagretSkjema.sluttDato}",
                      "startDato": "${lagretSkjema.startDato}",
                      "sendtInnTidspunkt": "${lagretSkjema.sendtInnTidspunkt}"
                    }
                    """,
                    strict = true
                )
            }
        }
    }

    @Test
    fun `GET skjemaV2 by id henter skjema lagret av noen andre men basert på rettighet`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        `when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf(
            AltinnOrganisasjon(organizationNumber = "1"),
        ))

        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                bedriftNr = "1",
                opprettetAv = "Noen Andrè",
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status().isOk
            content {
                json(
                    """
                    {
                      "id": "${lagretSkjema.id}",
                      "type": "${lagretSkjema.type}",
                      "bedriftNr": "${lagretSkjema.bedriftNr}",
                      "bedriftNavn": "${lagretSkjema.bedriftNavn}",
                      "kontaktNavn": "${lagretSkjema.kontaktNavn}",
                      "kontaktEpost": "${lagretSkjema.kontaktEpost}",
                      "kontaktTlf": "${lagretSkjema.kontaktTlf}",
                      "antallBerørt": ${lagretSkjema.antallBerørt},
                      "årsakskode": "${lagretSkjema.årsakskode}",
                      "årsakstekst": "${lagretSkjema.årsakskode.navn}",
                      "yrkeskategorier": [
                        {
                          "konseptId": ${lagretSkjema.yrkeskategorier.first().konseptId},
                          "label": "${lagretSkjema.yrkeskategorier.first().label}",
                          "styrk08": "${lagretSkjema.yrkeskategorier.first().styrk08}"
                        }
                      ],
                      "ukjentSluttDato": ${lagretSkjema.ukjentSluttDato},
                      "sluttDato": "${lagretSkjema.sluttDato}",
                      "startDato": "${lagretSkjema.startDato}",
                      "sendtInnTidspunkt": "${lagretSkjema.sendtInnTidspunkt}"
                    }
                    """,
                    strict = true
                )
            }
        }
    }

    @Test
    fun `GET skjemaV2 by id gir 404 når skjema lagret av noen andre og mangler bedrift basert på rettighet`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")
        `when`(altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")).thenReturn(listOf(
            AltinnOrganisasjon(organizationNumber = "1"),
        ))

        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                bedriftNr = "2",
                opprettetAv = "Noen Andrè",
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status().isNotFound
        }
    }

    @Test
    fun `POST skjemaV2 lagrer og returnerer og starter journalføring og kafka send`() {
        `when`(tokenUtil.autentisertBruker()).thenReturn("42")

        val lagretSkjema by lazy { repository.findAllByOpprettetAv("42").first() }

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
                  "antallBerørt": 12,
                  "årsakskode": "RÅSTOFFMANGEL",
                  "årsakstekst": "Råstoffmangel",
                  "type": "PERMITTERING_UTEN_LØNN",
                  "fritekst": "### Yrker\nKokkeassistent\n### Årsak\nRåstoffmangel"
                }
                """
        }.andExpect {
            status().isOk
            content {
                json(
                    """
                    {
                      "id": "${lagretSkjema.id}",
                      "type": "PERMITTERING_UTEN_LØNN",
                      "bedriftNr": "910825569",
                      "bedriftNavn": "STORFOSNA OG FREDRIKSTAD REGNSKAP",
                      "kontaktNavn": "asdf",
                      "kontaktEpost": "ken@g.no",
                      "kontaktTlf": "12341234",
                      "antallBerørt": 12,
                      "årsakskode": "RÅSTOFFMANGEL",
                      "årsakstekst": "Råstoffmangel",
                      "yrkeskategorier": [
                        {
                          "konseptId": 21837,
                          "label": "Kokkeassistent",
                          "styrk08": "5120.03"
                        }
                      ],
                      "ukjentSluttDato": false,
                      "sluttDato": "2023-12-14",
                      "startDato": "2023-12-09",
                      "sendtInnTidspunkt": "${lagretSkjema.sendtInnTidspunkt}"
                    }
                    """,
                    strict = true
                )
            }
        }

        verify(journalføringService).startJournalføring(skjemaid = lagretSkjema.id)
        verify(permitteringsmeldingKafkaService).scheduleSend(skjemaid = lagretSkjema.id)
    }
}

fun testSkjema(
    id: UUID = UUID.randomUUID(),
    type: PermitteringsskjemaType = PermitteringsskjemaType.PERMITTERING_UTEN_LØNN,
    bedriftNr: String = "999999999",
    bedriftNavn: String = "Bedrift AS",
    kontaktNavn: String = "Tore Toresen",
    kontaktEpost: String = "per@bedrift.no",
    kontaktTlf: String = "66778899",
    antallBerørt: Int = 42,
    årsakskode: Årsakskode = Årsakskode.MANGEL_PÅ_ARBEID,
    yrkeskategorier: List<YrkeskategoriV2> = listOf(
        YrkeskategoriV2(
            konseptId = 1000,
            styrk08 = "0001",
            label = "Label",
        )
    ),
    startDato: LocalDate = LocalDate.now().minusDays(1),
    sluttDato: LocalDate? = LocalDate.now(),
    ukjentSluttDato: Boolean = false,
    sendtInnTidspunkt: Instant = Instant.now(),
    opprettetAv: String = UUID.randomUUID().toString(),
) = PermitteringsskjemaV2(
    id = id,
    bedriftNr = bedriftNr,
    bedriftNavn = bedriftNavn,
    type = type,
    kontaktNavn = kontaktNavn,
    kontaktTlf = kontaktTlf,
    kontaktEpost = kontaktEpost,
    startDato = startDato,
    sluttDato = sluttDato,
    ukjentSluttDato = ukjentSluttDato,
    antallBerørt = antallBerørt,
    årsakskode = årsakskode,
    yrkeskategorier = yrkeskategorier,
    sendtInnTidspunkt = sendtInnTidspunkt,
    opprettetAv = opprettetAv,
)