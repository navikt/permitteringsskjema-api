package no.nav.permitteringsskjemaapi.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

private const val TOPIC = "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver"

@Service
class PermitteringsskjemaProdusent(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val mapper: ObjectMapper,
) {

    fun sendTilKafkaTopic(permitteringsskjema: Permitteringsskjema) {
        val rapport = PermitteringsskjemaKafkaMelding(
            antallBerorte = permitteringsskjema.antallBerørt!!,
            bedriftsnummer = permitteringsskjema.bedriftNr!!,
            fritekst = permitteringsskjema.fritekst!!,
            id = permitteringsskjema.id!!,
            kontaktEpost = permitteringsskjema.kontaktEpost!!,
            kontaktNavn = permitteringsskjema.kontaktNavn!!,
            kontaktTlf = permitteringsskjema.kontaktTlf!!,
            sendtInnTidspunkt = permitteringsskjema.sendtInnTidspunkt!!,
            sluttDato = permitteringsskjema.sluttDato,
            startDato = permitteringsskjema.startDato!!,
            varsletAnsattDato = permitteringsskjema.varsletAnsattDato!!,
            varsletNavDato = permitteringsskjema.varsletNavDato!!,
            type = permitteringsskjema.type!!,
            årsakskode = permitteringsskjema.årsakskode!!,
            årsakstekst = permitteringsskjema.årsakstekst,
            yrkeskategorier = permitteringsskjema.yrkeskategorier,
        )

        kafkaTemplate.send(
            ProducerRecord(
                TOPIC,
                rapport.id.toString(),
                mapper.writeValueAsString(rapport)
            )
        ).get(1, TimeUnit.SECONDS);
    }


    private data class PermitteringsskjemaKafkaMelding(
        var id: UUID,
        var bedriftsnummer: String,
        var sendtInnTidspunkt: Instant,
        var type: PermitteringsskjemaType,
        var kontaktNavn: String,
        var kontaktTlf: String,
        var kontaktEpost: String,
        var varsletAnsattDato: LocalDate,
        var varsletNavDato: LocalDate,
        var startDato: LocalDate,
        var sluttDato: LocalDate?,
        var fritekst: String,
        var antallBerorte: Int,
        var årsakskode: Årsakskode,
        var årsakstekst: String?,
        var yrkeskategorier: List<Yrkeskategori>,
    )
}