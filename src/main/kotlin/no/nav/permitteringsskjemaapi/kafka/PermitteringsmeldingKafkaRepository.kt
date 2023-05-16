package no.nav.permitteringsskjemaapi.kafka

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PermitteringsmeldingKafkaRepository : JpaRepository<PermitteringsmeldingKafkaEntry, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select r from PermitteringsmeldingKafkaEntry r order by r.queuePosition")
    fun fetchReplayQueueItems(pageable: Pageable): List<PermitteringsmeldingKafkaEntry>
}