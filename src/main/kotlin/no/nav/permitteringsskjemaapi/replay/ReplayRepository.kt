package no.nav.permitteringsskjemaapi.replay

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ReplayRepository : JpaRepository<ReplayQueueItem, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select r from ReplayQueueItem r")
    fun fetchReplayQueueItems(pageable: Pageable): List<ReplayQueueItem>
}