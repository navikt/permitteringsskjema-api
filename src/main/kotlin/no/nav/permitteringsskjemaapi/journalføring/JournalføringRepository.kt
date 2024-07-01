package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.*

interface JournalføringRepository : JpaRepository<Journalføring, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select jf from Journalføring jf
            where jf.state != 'FERDIG'
            and (jf.delayedUntil is null or :now >= jf.delayedUntil)
            order by jf.rowInsertedAt
            limit 1
    """)
    fun findWork(now: String = LocalDateTime.now().toString()) : Optional<Journalføring>

    @Query("""
        select jf.rowInsertedAt
        from Journalføring jf
        where jf.state != 'FERDIG'
        order by jf.rowInsertedAt
        limit 1
    """)
    fun oldestInsertionTimeNotCompleted(): Optional<String>
}