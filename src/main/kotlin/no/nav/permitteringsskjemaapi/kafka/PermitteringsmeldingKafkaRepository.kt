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

enum class QueueEventType { INNSENDT, TRUKKET }

@Entity
@Table(name = "deferred_kafka_queue")
class PermitteringsmeldingKafkaEntry() {

    constructor(skjemaId: UUID, eventType: QueueEventType) : this() {
        this.skjemaId = skjemaId
        this.eventType = eventType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: UUID? = null  // generated av DB

    @Column(name = "skjema_id", nullable = false)
    lateinit var skjemaId: UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    lateinit var eventType: QueueEventType

    @Column(name = "queue_position", insertable = false, updatable = false)
    var queuePosition: Int? = null
}