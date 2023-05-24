package no.nav.permitteringsskjemaapi.kafka

import jakarta.persistence.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PermitteringsmeldingKafkaRepository : JpaRepository<PermitteringsmeldingKafkaEntry, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select r from PermitteringsmeldingKafkaEntry r order by r.queuePosition")
    fun fetchQueueItems(pageable: Pageable): List<PermitteringsmeldingKafkaEntry>
}

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
    var queuePosition: Int? = null

    override fun equals(other: Any?) =
        other is PermitteringsmeldingKafkaEntry && this.skjemaId == other.skjemaId

    override fun hashCode() = skjemaId.hashCode()
}