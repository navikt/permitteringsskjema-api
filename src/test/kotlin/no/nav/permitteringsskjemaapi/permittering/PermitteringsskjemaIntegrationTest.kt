package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.altinn.Organisasjon
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaIntegrationTest.Companion.`En annen BEDR`
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import org.assertj.core.api.Assertions
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

val testOrganisasjon = Organisasjon(
    name = "",
    parentOrganizationNumber = "",
    organizationNumber = "",
    organizationForm = "",
)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermitteringsskjemaIntegrationTest  {
    companion object {
        /* Marte eier en bedrift, og har alle rettigheter i den. */
        const val `Marte med lesetilganger` = "22222222222"
        val `Martes første BEDR` = testOrganisasjon.copy(organizationNumber = "11111111")
        val `Martes andre BEDR` = testOrganisasjon.copy(organizationNumber = "2222222")

        /* Unni jobber i Marte sin bedrift, så har "vilkårlig reportee-tilgang", men har ikke
         * enkteltrettigheten for å kunne lese innsende skjemaer. */
        const val `Unni uten lesetilganger` = "11111111111"

         /* Helle jobber et helt annet sted, og har tilganger i sin bedrift, men ingen i Marte sin. */
        const val `Helle helt annen person` = "33333333333"
        val `En annen BEDR` = testOrganisasjon.copy(organizationNumber = "999999999")
    }

    @TestConfiguration
    class Config {
        @Bean
        @Primary
        fun altinnService(authenticatedUserHolder: AuthenticatedUserHolder) = object: AltinnService {
            override fun hentOrganisasjoner(): List<Organisasjon> = when(val fnr = authenticatedUserHolder.fnr) {
                `Unni uten lesetilganger` -> listOf(`Martes første BEDR`, `Martes andre BEDR`)
                `Marte med lesetilganger` -> listOf(`Martes første BEDR`, `Martes andre BEDR`)
                `Helle helt annen person` -> listOf(`En annen BEDR`)
                else -> throw NotImplementedError("Fnr ikke brukt i testen: $fnr")
            }

            override fun hentOrganisasjonerBasertPåRettigheter(
                serviceKode: String,
                serviceEdition: String
            ): List<Organisasjon> {
                require(serviceKode == "5810" && serviceEdition == "1")
                return when (val fnr = authenticatedUserHolder.fnr) {
                    `Unni uten lesetilganger` -> emptyList()
                    `Marte med lesetilganger` -> listOf(`Martes første BEDR`, `Martes andre BEDR`)
                    `Helle helt annen person` -> listOf(`En annen BEDR`)
                    else -> throw NotImplementedError("Fnr ikke brukt i testen: $fnr")
                }
            }
        }


    }

    @MockBean
    lateinit var journalføringService: JournalføringService

    @MockBean
    lateinit var permitteringsmeldingKafkaService: PermitteringsmeldingKafkaService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repository: PermitteringsskjemaRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
    }

    val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)!!

    var client: HttpClient = HttpClient.newHttpClient()

    fun MockHttpServletRequestDsl.token(pid: String) {
        val response = client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:9000/tokenx/token"))
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(
                    HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=1234&client_secret=1234&scope=$pid"))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        val token = objectMapper.readTree(response.body())["access_token"].textValue()
        header("Authorization", "Bearer $token")
    }

    @Test
    fun `GET skjemaV2 henter alle skjema sortert`() {
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 1 min siden",
                bedriftNr = `Martes første BEDR`.organizationNumber,
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES),
                opprettetAv = `Unni uten lesetilganger`,
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 5 min siden",
                bedriftNr = `Martes andre BEDR`.organizationNumber,
                sendtInnTidspunkt = now.minus(5, ChronoUnit.MINUTES),
                opprettetAv = `Unni uten lesetilganger`,
            ),
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 10 min siden",
                bedriftNr = `En annen BEDR`.organizationNumber,
                sendtInnTidspunkt = now.minus(10, ChronoUnit.MINUTES),
                opprettetAv = `Marte med lesetilganger`,
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 2 min siden",
                bedriftNr = `En annen BEDR`.organizationNumber,
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                opprettetAv = `Marte med lesetilganger`,
            )
        )

        val jsonResponse = mockMvc.get("/skjemaV2") {
            accept(MediaType.APPLICATION_JSON)
            token(`Marte med lesetilganger`)
        }.andExpect {
            status {
                isOk()
            }
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
        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                opprettetAv = `Unni uten lesetilganger`,
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
            token(`Unni uten lesetilganger`)
        }.andExpect {
            status {
                isOk()
            }
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
        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                bedriftNr = `Martes første BEDR`.organizationNumber,
                opprettetAv = `Unni uten lesetilganger`,
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
            token(`Marte med lesetilganger`)
        }
        .andExpect {
            status {
                isOk()
            }
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
        val lagretSkjema = repository.save(
            testSkjema(
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                bedriftNr = `En annen BEDR`.organizationNumber,
                opprettetAv = `Helle helt annen person`,
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(MediaType.APPLICATION_JSON)
            token(`Marte med lesetilganger`)
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `POST skjemaV2 lagrer og returnerer og starter journalføring og kafka send`() {
        val lagretSkjema by lazy { repository.findAllByOpprettetAv(`Unni uten lesetilganger`).first() }

        mockMvc.post("/skjemaV2") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            token(`Unni uten lesetilganger`)
            content = """
                {
                  "yrkeskategorier": [
                    {
                      "konseptId": 21837,
                      "label": "Kokkeassistent",
                      "styrk08": "5120.03"
                    }
                  ],
                  "bedriftNr": "${`Martes andre BEDR`.organizationNumber}",
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
            status {
                isOk()
            }
            content {
                json(
                    """
                    {
                      "id": "${lagretSkjema.id}",
                      "type": "PERMITTERING_UTEN_LØNN",
                      "bedriftNr": "${`Martes andre BEDR`.organizationNumber}",
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

    @Test
    fun `POST skjemaV2 returnerer 403 hvis bruker ikke har tilgang til virksomhet`() {
        mockMvc.post("/skjemaV2") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            token(`Helle helt annen person`)
            content = """
                {
                  "yrkeskategorier": [
                    {
                      "konseptId": 21837,
                      "label": "Kokkeassistent",
                      "styrk08": "5120.03"
                    }
                  ],
                  "bedriftNr": "${`Martes andre BEDR`.organizationNumber}",
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
            status {
                isForbidden()
            }
        }
    }
}

fun testSkjema(
    id: UUID = UUID.randomUUID(),
    type: SkjemaType = SkjemaType.PERMITTERING_UTEN_LØNN,
    bedriftNr: String = `En annen BEDR`.organizationNumber,
    bedriftNavn: String = "Bedrift AS",
    kontaktNavn: String = "Tore Toresen",
    kontaktEpost: String = "per@bedrift.no",
    kontaktTlf: String = "66778899",
    antallBerørt: Int = 42,
    årsakskode: Årsakskode = Årsakskode.MANGEL_PÅ_ARBEID,
    yrkeskategorier: List<Yrkeskategori> = listOf(
        Yrkeskategori(
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
) = Permitteringsskjema(
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