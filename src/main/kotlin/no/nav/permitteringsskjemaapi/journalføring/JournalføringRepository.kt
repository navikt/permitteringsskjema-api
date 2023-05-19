package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface JournalføringRepository : JpaRepository<Journalføring, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select jf from Journalføring jf
            where jf.state != 'FERDIG'
            order by jf.rowInsertedAt
            limit 1
    """)
    fun findWork() : Optional<Journalføring>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select jf from Journalføring jf
            where jf.state != 'FERDIG'
            order by jf.rowInsertedAt
    """)
    fun findWorks(pageable: Pageable) : List<Journalføring>
}