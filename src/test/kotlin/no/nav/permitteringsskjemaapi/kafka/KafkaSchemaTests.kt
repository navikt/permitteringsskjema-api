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
        assertNotValid("[]")
    }

    private fun assertValid(json: String) {
        val errors = kafkaProdusent.validateAgainstSchema(json)
        if (errors.isNotEmpty()) {
            fail("schema validation failed: ${errors.joinToString()}")
        }
    }

    private fun assertNotValid(json: String) {
        val errors = kafkaProdusent.validateAgainstSchema(json)
        if (errors.isEmpty()) {
            fail("schema validation succeeded, but expected it to fail")
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
}