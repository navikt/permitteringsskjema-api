package no.nav.permitteringsskjemaapi.kafka

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CompletableFuture


@SpringBootTest
@ActiveProfiles("test")
class PermitteringsskjemaProdusentTest {

    @MockitoBean
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    lateinit var permitteringsskjemaProdusent: PermitteringsskjemaProdusent

    @Suppress("UNCHECKED_CAST")
    @Test
    fun senderTilKafka() {
        val captor = ArgumentCaptor.forClass(ProducerRecord::class.java) as ArgumentCaptor<ProducerRecord<String, String>>
        `when`(kafkaTemplate.send(captor.capture())).thenReturn(CompletableFuture.completedFuture(null))
        permitteringsskjemaProdusent.sendTilKafkaTopic(
            Permitteringsskjema(
                id = UUID.randomUUID(),
                antallBerørt = 1,
                bedriftNavn = "hey",
                bedriftNr = "1234567890",
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
        )

        val json = captor.value.value()
        Assertions.assertNotNull(json)
    }
}