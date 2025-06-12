package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.SkedulerPermitteringsmeldingService
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.tokenx.TokenXToken
import org.assertj.core.api.Assertions
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.json.JsonCompareMode.LENIENT
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermitteringsskjemaIntegrationTest {

    @MockitoBean
    lateinit var journalføringService: JournalføringService

    @MockitoBean
    lateinit var skedulerPermitteringsmeldingService: SkedulerPermitteringsmeldingService

    @MockitoBean
    lateinit var tokenExchangeClient: TokenExchangeClient

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repository: PermitteringsskjemaRepository

    val objectMapper = ObjectMapper()

    @Autowired
    lateinit var flyway: Flyway

    @Autowired
    lateinit var altinnService: AltinnService

    lateinit var altinnTilgangerServer: MockRestServiceServer


    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
        altinnTilgangerServer = MockRestServiceServer.bindTo(altinnService.restTemplate).build()
        `when`(tokenExchangeClient.exchange(anyString(), anyString()))
            .then {
                TokenXToken(access_token = it.arguments[0] as String)
            }
    }

    val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)!!

    var client: HttpClient = HttpClient.newHttpClient()

    /* Marte eier en bedrift, og har alle rettigheter i den. */
    private val `Marte med lesetilganger` = "22222222222"

    /* Unni jobber i Marte sin bedrift, så har "vilkårlig reportee-tilgang", men har ikke
     * enkteltrettigheten for å kunne lese innsende skjemaer. */
    private val `Unni uten lesetilganger` = "11111111111"

    /* Helle jobber et helt annet sted, og har tilganger i sin bedrift, men ingen i Marte sin. */
    private val `Helle helt annen person` = "33333333333"

    private val `En annen BEDR` = "1"
    private val `En annen AS` = "2"
    private val `Martes første BEDR` = "3"
    private val `Martes andre BEDR` = "4"
    private val `Martes overenhet` = "5"

    //language=JSON
    val `Martes tilganger` = """
        {
            "hierarki": [
                {
                    "orgnr": "$`Martes overenhet`",
                    "navn": "marthes overenhet",
                    "organisasjonsform": "AS",
                    "underenheter": [
                        {
                            "orgnr": "$`Martes første BEDR`",
                            "underenheter": [],
                            "navn": "marthes første bedrift",
                            "organisasjonsform": "BEDR"
                        },
                        {
                            "orgnr": "$`Martes andre BEDR`",
                            "underenheter": [],
                            "navn": "marthes andre bedrift",
                            "organisasjonsform": "BEDR"
                        }
                    ]
                }
            ],
            "isError": false,
            "orgNrTilTilganger": {
                "$`Martes første BEDR`": ["$INNSYN_ALLE_PERMITTERINGSSKJEMA"],
                "$`Martes andre BEDR`": ["$INNSYN_ALLE_PERMITTERINGSSKJEMA"]
            },
            "tilgangTilOrgNr": {
                "$INNSYN_ALLE_PERMITTERINGSSKJEMA": ["$`Martes første BEDR`", "$`Martes andre BEDR`"]
            }
        }
    """

    //language=JSON
    val `Unnis tilganger` = """
        {
            "hierarki": [
                {
                    "orgnr": "$`Martes overenhet`",
                    "navn": "marthes overenhet",
                    "organisasjonsform": "AS",
                    "underenheter": [
                        {
                            "orgnr": "$`Martes første BEDR`",
                            "underenheter": [],
                            "navn": "marthes første bedrift",
                            "organisasjonsform": "BEDR"
                        },
                        {
                            "orgnr": "$`Martes andre BEDR`",
                            "underenheter": [],
                            "navn": "marthes andre bedrift",
                            "organisasjonsform": "BEDR"
                        }
                    ]
                }
            ],
            "isError": false,
            "orgNrTilTilganger": {},
            "tilgangTilOrgNr": {}
        }
    """

    //language=JSON
    val `Helles tilganger` = """
        {
            "hierarki": [
                {
                    "orgnr": "$`En annen AS`",
                    "navn": "En annen AS",
                    "organisasjonsform": "AS",
                    "underenheter": [
                        {
                            "orgnr": "$`En annen BEDR`",
                            "navn": "En annen BEDR",
                            "organisasjonsform": "BEDR",
                            "underenheter": []
                        }
                    ]
                }
            ],
            "isError": false,
            "orgNrTilTilganger": {
                "$`En annen BEDR`": ["$INNSYN_ALLE_PERMITTERINGSSKJEMA"]
            },
            "tilgangTilOrgNr": {
                "$INNSYN_ALLE_PERMITTERINGSSKJEMA": ["$`En annen BEDR`"]
            }
        }
    """
    val `martes token` = token(`Marte med lesetilganger`)
    val `unnis token` = token(`Unni uten lesetilganger`)
    val `helles token` = token(`Helle helt annen person`)

    @Test
    fun `GET skjemaV2 henter alle skjema sortert`() {
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 1 min siden",
                bedriftNr = `Martes første BEDR`,
                sendtInnTidspunkt = now.minus(1, ChronoUnit.MINUTES),
                opprettetAv = `Unni uten lesetilganger`,
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 5 min siden",
                bedriftNr = `Martes andre BEDR`,
                sendtInnTidspunkt = now.minus(5, ChronoUnit.MINUTES),
                opprettetAv = `Unni uten lesetilganger`,
            ),
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 10 min siden",
                bedriftNr = `En annen BEDR`,
                sendtInnTidspunkt = now.minus(10, ChronoUnit.MINUTES),
                opprettetAv = `Marte med lesetilganger`,
            )
        )
        repository.save(
            testSkjema(
                bedriftNavn = "sendt inn 2 min siden",
                bedriftNr = `En annen BEDR`,
                sendtInnTidspunkt = now.minus(2, ChronoUnit.MINUTES),
                opprettetAv = `Marte med lesetilganger`,
            )
        )
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`martes token`")
        }.andRespond(
            withSuccess(
                `Martes tilganger`,
                APPLICATION_JSON
            )
        )

        val jsonResponse = mockMvc.get("/skjemaV2") {
            accept(APPLICATION_JSON)
            header("Authorization", "Bearer $`martes token`")
        }.andExpect {
            status {
                isOk()
            }
        }.andReturn().response.contentAsString

        val jsonNode: JsonNode = objectMapper.readValue(jsonResponse)
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
                bedriftNr = `En annen BEDR`,
            )
        )
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`unnis token`")
        }.andRespond(
            withSuccess(
                `Unnis tilganger`,
                APPLICATION_JSON
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(APPLICATION_JSON)
            header("Authorization", "Bearer $`unnis token`")
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
                bedriftNr = `Martes første BEDR`,
                opprettetAv = `Unni uten lesetilganger`,
            )
        )
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`martes token`")
        }.andRespond(
            withSuccess(
                `Martes tilganger`,
                APPLICATION_JSON
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(APPLICATION_JSON)
            header("Authorization", "Bearer $`martes token`")
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
                bedriftNr = `En annen BEDR`,
                opprettetAv = `Helle helt annen person`,
            )
        )
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`martes token`")
        }.andRespond(
            withSuccess(
                `Martes tilganger`,
                APPLICATION_JSON
            )
        )

        mockMvc.get("/skjemaV2/${lagretSkjema.id}") {
            accept(APPLICATION_JSON)
            header("Authorization", "Bearer $`martes token`")
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `POST skjemaV2 lagrer og returnerer og starter journalføring og kafka send`() {
        val lagretSkjema by lazy { repository.findAllByOpprettetAv(`Unni uten lesetilganger`).first() }

        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`unnis token`")
        }.andRespond(
            withSuccess(
                `Unnis tilganger`,
                APPLICATION_JSON
            )
        )
        mockMvc.post("/skjemaV2") {
            accept = APPLICATION_JSON
            contentType = APPLICATION_JSON
            header("Authorization", "Bearer $`unnis token`")
            content = """
                {
                  "yrkeskategorier": [
                    {
                      "konseptId": 21837,
                      "label": "Kokkeassistent",
                      "styrk08": "5120.03"
                    }
                  ],
                  "bedriftNr": "$`Martes andre BEDR`",
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
                      "bedriftNr": "$`Martes andre BEDR`",
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
                    LENIENT
                )
            }
        }

        verify(journalføringService).startJournalføring(skjemaid = lagretSkjema.id)
        verify(skedulerPermitteringsmeldingService).scheduleSend(skjemaid = lagretSkjema.id)
    }

    @Test
    fun `POST skjemaV2 returnerer 403 hvis bruker ikke har tilgang til virksomhet`() {
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`helles token`")
        }.andRespond(
            withSuccess(
                `Helles tilganger`,
                APPLICATION_JSON
            )
        )
        mockMvc.post("/skjemaV2") {
            accept = APPLICATION_JSON
            contentType = APPLICATION_JSON
            header("Authorization", "Bearer $`helles token`")
            content = """
                {
                  "yrkeskategorier": [
                    {
                      "konseptId": 21837,
                      "label": "Kokkeassistent",
                      "styrk08": "5120.03"
                    }
                  ],
                  "bedriftNr": "$`Martes andre BEDR`",
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

    @Test
    fun `GET organisasjoner-v2 returnere trestruktur`() {
        altinnTilgangerServer.expect {
            requestTo("http://arbeidsgiver-altinn-tilganger.fager/altinn-tilganger")
            method(POST)
            header("Authorization", "Bearer $`martes token`")
        }.andRespond(
            withSuccess(
                `Martes tilganger`,
                APPLICATION_JSON
            )
        )
        mockMvc.get("/organisasjoner-v2") {
            accept(APPLICATION_JSON)
            header("Authorization", "Bearer $`martes token`")
        }.andExpect {
            status {
                isOk()
            }
            content {
                json(
                    """
            [{
              "orgnr": "$`Martes overenhet`",
              "navn": "marthes overenhet",
              "organisasjonsform": "AS",
              "underenheter": [
                {
                  "orgnr": "$`Martes første BEDR`",
                  "underenheter": [],
                  "navn": "marthes første bedrift",
                  "organisasjonsform": "BEDR"
                },
                {
                  "orgnr": "$`Martes andre BEDR`",
                  "underenheter": [],
                  "navn": "marthes andre bedrift",
                  "organisasjonsform": "BEDR"
                }
              ]
            }]
            """,
                    LENIENT
                )
            }
        }
    }

    private fun token(pid: String): String {
        val response = client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:9100/tokenx/token"))
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(
                    HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=1234&client_secret=1234&scope=$pid")
                )
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        return objectMapper.readTree(response.body())["access_token"].textValue()!!
    }

}

fun testSkjema(
    id: UUID = UUID.randomUUID(),
    type: SkjemaType = SkjemaType.PERMITTERING_UTEN_LØNN,
    bedriftNr: String,
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
