package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.*

interface JournalføringRepository : JpaRepository<Journalføring, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    /* Denne "order by" forhindrer starvation, i motsetning til en sortering på kun
     * delayedUntil eller rowInsertedAt. */
    @Query("""
        select jf from Journalføring jf
            where jf.state != 'FERDIG'
            and (jf.delayedUntil is null or :now >= jf.delayedUntil)
            order by jf.delayedUntil nulls first, jf.rowInsertedAt nulls first
            limit 1
    """)
    fun findWork(now: Instant = Instant.now()) : Optional<Journalføring>

    @Query("""
        select jf.rowInsertedAt
        from Journalføring jf
        where jf.state != 'FERDIG'
        order by jf.rowInsertedAt
        limit 1
    """)
    fun oldestInsertionTimeNotCompleted(): Optional<String>
}