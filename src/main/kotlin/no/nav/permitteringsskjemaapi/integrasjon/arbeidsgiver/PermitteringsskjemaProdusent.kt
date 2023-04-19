package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.permitteringsskjemaapi.config.NAV_CALL_ID
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.MDC
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.*

@Service
class PermitteringsskjemaProdusent(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val mapper: ObjectMapper,
    private val context: ApplicationContext
) {
    private val topic = "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver"
    private val log = logger()

    fun sendInn(permitteringsskjema: Permitteringsskjema) {
        sendRapport(permitteringsskjema)
    }

    private fun publiser(rapport: ArbeidsgiverRapport) {
        log.info("Legger permitteringsskjema {} på kø", rapport.id)
        val record = ProducerRecord(topic, rapport.id.toString(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rapport))
        record.headers().add(
            RecordHeader(NAV_CALL_ID, (MDC.get(NAV_CALL_ID) ?: UUID.randomUUID().toString()).toByteArray())
        )
        send(record)
    }

    private fun sendRapport(skjema: Permitteringsskjema) {
        val rapport = ArbeidsgiverRapport(
            antallBerorte = skjema.antallBerørt,
            bedriftsnummer = skjema.bedriftNr,
            fritekst = skjema.fritekst,
            id = skjema.id,
            kontaktEpost = skjema.kontaktEpost,
            kontaktNavn = skjema.kontaktNavn,
            kontaktTlf = skjema.kontaktTlf,
            sendtInnTidspunkt = skjema.sendtInnTidspunkt,
            sluttDato = skjema.sluttDato,
            startDato = skjema.startDato,
            varsletAnsattDato = skjema.varsletAnsattDato,
            varsletNavDato = skjema.varsletNavDato,
            type = skjema.type,
            årsakskode = skjema.årsakskode,
            årsakstekst = skjema.årsakstekst,
            yrkeskategorier = skjema.yrkeskategorier,
        )
        publiser(rapport)
    }

    private fun send(record: ProducerRecord<String?, String?>) {
        log.debug("Sender melding {} på {}", record.value(), topic)
        kafkaTemplate.send(record)
            .whenComplete { result: SendResult<String?, String?>, e: Throwable? ->
                if (e == null) {
                    log.info("Sendte melding på {}", topic)
                    log.debug(
                        "Sendte melding {} med offset {} på {}", record.value(),
                        result.recordMetadata.offset(), topic
                    )
                } else {
                    log.error(
                        "Kunne ikke sende melding på {}. Dette er kanskje på grunn av rullerte credentials. Appen stoppes.",
                        topic,
                        e
                    )
                    SpringApplication.exit(context)
                }
            }
    }
}