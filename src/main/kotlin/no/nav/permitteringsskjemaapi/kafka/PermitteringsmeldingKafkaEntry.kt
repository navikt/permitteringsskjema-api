package no.nav.permitteringsskjemaapi.kafka

import jakarta.annotation.Generated
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenerationTime
import java.awt.print.Book
import java.util.*


@Entity
@Table(name = "deferred_kafka_queue")
class PermitteringsmeldingKafkaEntry() {
    constructor(skjemaId: UUID): this() {
        this.skjemaId = skjemaId
    }

    @field:Id
    @field:Column(name = "skjema_id")
    lateinit var skjemaId: UUID

    @field:Column(name = "queue_position", insertable = false)
//    @field:GeneratedValue(GenerationTime.INSERT)
    var queuePosition: Int? = null

    override fun equals(other: Any?) =
        other is PermitteringsmeldingKafkaEntry && this.skjemaId == other.skjemaId

    override fun hashCode() = skjemaId.hashCode()
}