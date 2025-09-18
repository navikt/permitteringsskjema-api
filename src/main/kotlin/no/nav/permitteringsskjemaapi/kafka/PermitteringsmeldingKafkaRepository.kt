package no.nav.permitteringsskjemaapi.kafka

import jakarta.persistence.*
import no.nav.permitteringsskjemaapi.permittering.HendelseType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PermitteringsmeldingKafkaRepository : JpaRepository<PermitteringsmeldingKafkaEntry, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select r from PermitteringsmeldingKafkaEntry r order by r.queuePosition")
    fun fetchQueueItems(pageable: Pageable): List<PermitteringsmeldingKafkaEntry>

    fun existsBySkjemaIdAndHendelseType(skjemaId: UUID, hendelseType: HendelseType): Boolean
}

@Entity
@Table(name = "deferred_kafka_queue")
class PermitteringsmeldingKafkaEntry() {

    constructor(skjemaId: UUID, hendelseType: HendelseType) : this() {
        this.skjemaId = skjemaId
        this.hendelseType = hendelseType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: UUID? = null  // generated av DB

    @Column(name = "skjema_id", nullable = false)
    lateinit var skjemaId: UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "hendelse_type", nullable = false)
    lateinit var hendelseType: HendelseType

    @Column(name = "queue_position", insertable = false, updatable = false)
    var queuePosition: Int? = null

    override fun equals(other: Any?): Boolean =
        other is PermitteringsmeldingKafkaEntry &&
            skjemaId == other.skjemaId &&
            hendelseType == other.hendelseType

    override fun hashCode(): Int = 31 * skjemaId.hashCode() + hendelseType.hashCode()
}
