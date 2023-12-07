package no.nav.permitteringsskjemaapi.permittering

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

val fromDate = LocalDateTime.now().minusYears(2).truncatedTo(ChronoUnit.DAYS).toInstant(ZoneOffset.UTC)

interface PermitteringsskjemaRepository : JpaRepository<Permitteringsskjema, UUID> {
    fun findByIdAndOpprettetAv(id: UUID, opprettetAv: String): Optional<Permitteringsskjema>
    override fun findById(id: UUID): Optional<Permitteringsskjema>

    @Query(
        "select s from Permitteringsskjema s " +
                "   where s.opprettetAv = :opprettetAv " +
                "   order by s.sendtInnTidspunkt desc nulls last, s.opprettetTidspunkt desc"
    )
    fun findAllByOpprettetAv(opprettetAv: String): List<Permitteringsskjema>

    @Query(
        "select s from Permitteringsskjema s " +
                "   where s.bedriftNr = :bedriftNr " +
                "   and s.sendtInnTidspunkt is not null" +
                "   and s.sendtInnTidspunkt > :fraDato" +
                "   order by s.sendtInnTidspunkt desc, s.opprettetTidspunkt desc"
    )
    fun findAllByBedriftNr(
        bedriftNr: String,
        fraDato: Instant = fromDate
    ): List<Permitteringsskjema>

    @Query(
        "select s from Permitteringsskjema s " +
                "   where s.opprettetAv = :opprettetAv " +
                "   and s.sendtInnTidspunkt is not null" +
                "   and s.sendtInnTidspunkt > :fraDato" +
                "   order by s.sendtInnTidspunkt desc"
    )
    fun findAllInnsendteByOpprettetAv(
        opprettetAv: String,
        fraDato: Instant = fromDate
    ): List<Permitteringsskjema>
}