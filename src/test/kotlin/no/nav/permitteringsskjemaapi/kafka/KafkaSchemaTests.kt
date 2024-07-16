package no.nav.permitteringsskjemaapi.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.time.LocalDate
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class KafkaSchemaTests {
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
            fail<Unit>("schema validation failed: ${errors.joinToString()}")
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
        id = UUID.randomUUID(),
        sendtInnTidspunkt = Instant.now(),
        sluttDato = LocalDate.now().plusDays(30),
        startDato = LocalDate.now(),
        type = SkjemaType.INNSKRENKNING_I_ARBEIDSTID,
        yrkeskategorier = listOf(
            Yrkeskategori(
                konseptId = 42,
                styrk08 = "1",
                label = "foo",
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