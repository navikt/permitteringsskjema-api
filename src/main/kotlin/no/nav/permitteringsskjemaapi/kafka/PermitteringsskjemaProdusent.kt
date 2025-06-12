package no.nav.permitteringsskjemaapi.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.SkjemaType
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

    fun sendTilKafkaTopic(permitteringsskjema: Permitteringsskjema) : Unit {
        val rapport = PermitteringsskjemaKafkaMelding(
            antallBerorte = permitteringsskjema.antallBerørt,
            bedriftsnummer = permitteringsskjema.bedriftNr,
            id = permitteringsskjema.id,
            sendtInnTidspunkt = permitteringsskjema.sendtInnTidspunkt,
            sluttDato = permitteringsskjema.sluttDato,
            startDato = permitteringsskjema.startDato,
            type = permitteringsskjema.type,
            årsakskode = permitteringsskjema.årsakskode,
            årsakstekst = permitteringsskjema.årsakstekst,
            yrkeskategorier = permitteringsskjema.yrkeskategorier,
        )

        val jsonEvent = mapper.writeValueAsString(rapport)

        val validationErrors = validateAgainstSchema(jsonEvent)
        if (validationErrors.isNotEmpty()) {
            throw Error("json validation failed: ${validationErrors.joinToString()}")
        }

        kafkaTemplate.send(
            ProducerRecord(
                TOPIC,
                rapport.id.toString(),
                jsonEvent
            )
        ).get(1, TimeUnit.SECONDS)
    }


    data class PermitteringsskjemaKafkaMelding(
        var id: UUID,
        var bedriftsnummer: String,
        var sendtInnTidspunkt: Instant,
        var type: SkjemaType,
        var startDato: LocalDate,
        var sluttDato: LocalDate?,
        var antallBerorte: Int,
        var årsakskode: Årsakskode,
        var årsakstekst: String?,
        var yrkeskategorier: List<Yrkeskategori>,
    )

    private val jsonSchema = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("kafka-schema.json")
        .use {
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(it)
                .apply {
                    initializeValidators()
                }
        }

    fun validateAgainstSchema(json: String) =
        jsonSchema.validate(mapper.readTree(json))
}