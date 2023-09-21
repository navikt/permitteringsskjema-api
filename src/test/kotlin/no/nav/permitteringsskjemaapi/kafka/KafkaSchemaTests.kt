package no.nav.permitteringsskjemaapi.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.LocalDate
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(properties = [
    "spring.flyway.cleanDisabled=false",
    "spring.flyway.validateOnMigrate=false"
])
@MockBean(MultiIssuerConfiguration::class)
@ActiveProfiles("test")
@DirtiesContext
class KafkaSchemaTests {
    @MockBean
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var kafkaProdusent: PermitteringsskjemaProdusent

    @Test
    fun validererEksempel1() {
        assertValid(eksempel1)
    }

    @Test
    fun validererEksempel2() {
        assertValid(eksempel2)
    }

    @Test
    fun godtarIkkeHvaSomHelst() {
        assertNotValid("$", "[]")
    }

    @Test
    fun godtarIkkeFeilDatoFormat() {
        assertNotValid("\$.startDato", eksempelFeilDatoFormat)
    }

    private fun assertValid(json: String) {
        val errors = kafkaProdusent.validateAgainstSchema(json)
        if (errors.isNotEmpty()) {
            fail("schema validation failed: ${errors.joinToString()}")
        }
    }

    private fun assertNotValid(path: String, json: String) {
        val errors = kafkaProdusent.validateAgainstSchema(json)
        if (errors.isEmpty()) {
            fail("schema validation succeeded, but expected it to fail")
        } else if (errors.all { it.path != path}) {
            fail("expected schema to fail on path $path, but only errors for ${errors.joinToString { it.path }}")
        } else {
            println("got expected validation errors for path $path: ${errors.joinToString()}")
        }
    }

    private fun assertValid(skjema: PermitteringsskjemaProdusent.PermitteringsskjemaKafkaMelding) =
        assertValid(mapper.writeValueAsString(skjema))


    private val eksempel1 = PermitteringsskjemaProdusent.PermitteringsskjemaKafkaMelding(
        antallBerorte = 10,
        bedriftsnummer = "123412341",
        fritekst = "friiiiii",
        id = UUID.randomUUID(),
        kontaktEpost = "lol@lol",
        kontaktNavn = "dora",
        kontaktTlf = "43214123",
        sendtInnTidspunkt = Instant.now(),
        sluttDato = LocalDate.now().plusDays(30),
        startDato = LocalDate.now(),
        type = PermitteringsskjemaType.INNSKRENKNING_I_ARBEIDSTID,
        varsletAnsattDato = LocalDate.now().minusDays(7),
        varsletNavDato = LocalDate.now(),
        yrkeskategorier = mutableListOf(
            Yrkeskategori(
                id = UUID.randomUUID(),
                permitteringsskjema = null,
                konseptId = 42,
                styrk08 = "1",
                label = "foo",
                antall = 3,
            )
        ),
        årsakskode = Årsakskode.ANDRE_ÅRSAKER,
        årsakstekst = "bare fordi",
    )

    private val eksempel2 = eksempel1.copy(sluttDato = null)


    private val eksempelFeilDatoFormat = """
        {
          "id": "785773df-8ec7-43cb-a252-bdcba68ec0bc",
          "bedriftsnummer": "123412341",
          "sendtInnTidspunkt": "2023-09-21T12:56:03.840448Z",
          "type": "PERMITTERING_UTEN_LØNN",
          "kontaktNavn": "asdfasdf",
          "kontaktTlf": "12341234",
          "kontaktEpost": "asdf@asdf.no",
          "varsletAnsattDato": "2023-09-21",
          "varsletNavDato": "2023-09-21",
          "startDato": "2023-19-22",
          "fritekst": "\n### Årsak\nRåstoffmangel\n### Yrker\nSushikokk\n### Annet\n",
          "antallBerorte": 3,
          "årsakskode": "RÅSTOFFMANGEL",
          "yrkeskategorier": [
            {
              "konseptId": 21838,
              "styrk08": "5120.03",
              "label": "Sushikokk"
            }
          ]
        }
    """
}