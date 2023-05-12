package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.permitteringsskjemaapi.config.NAV_CALL_ID
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.MDC
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.*

@Service
class PermitteringsskjemaProdusent(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val mapper: ObjectMapper,
) {
    private val topic = "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver"
    private val log = logger()

    fun sendInn(permitteringsskjema: Permitteringsskjema) {
        val rapport = ArbeidsgiverRapport(
            antallBerorte = permitteringsskjema.antallBerørt,
            bedriftsnummer = permitteringsskjema.bedriftNr,
            fritekst = permitteringsskjema.fritekst,
            id = permitteringsskjema.id,
            kontaktEpost = permitteringsskjema.kontaktEpost,
            kontaktNavn = permitteringsskjema.kontaktNavn,
            kontaktTlf = permitteringsskjema.kontaktTlf,
            sendtInnTidspunkt = permitteringsskjema.sendtInnTidspunkt,
            sluttDato = permitteringsskjema.sluttDato,
            startDato = permitteringsskjema.startDato,
            varsletAnsattDato = permitteringsskjema.varsletAnsattDato,
            varsletNavDato = permitteringsskjema.varsletNavDato,
            type = permitteringsskjema.type,
            årsakskode = permitteringsskjema.årsakskode,
            årsakstekst = permitteringsskjema.årsakstekst,
            yrkeskategorier = permitteringsskjema.yrkeskategorier,
        )

        kafkaTemplate.send(
            ProducerRecord(
                topic,
                rapport.id.toString(),
                mapper.writeValueAsString(rapport)
            ).apply<ProducerRecord<String, String>> {
                headers().add(
                    RecordHeader(NAV_CALL_ID, (MDC.get(NAV_CALL_ID) ?: UUID.randomUUID().toString()).toByteArray())
                )
            }
        ).whenComplete { _: SendResult<String?, String?>, e: Throwable? ->
            if (e != null) {
                log.error("Kunne ikke sende melding på {}.", topic, e)
            }
        }
    }
}