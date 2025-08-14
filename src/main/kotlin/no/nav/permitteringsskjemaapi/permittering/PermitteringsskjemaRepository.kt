package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Repository
class PermitteringsskjemaRepository(
    val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    val objectMapper: ObjectMapper,
) {
    
    fun findById(id: UUID): Permitteringsskjema? {
        return namedParameterJdbcTemplate.queryForList(
                """
                    select * 
                    from permitteringsskjema_v2
                    where id = :id
                """,
                mapOf("id" to id)
            ).firstOrNull()?.toPermitteringsskjema()
    }

    fun findByIdAndOpprettetAv(id: UUID, opprettetAv: String): Permitteringsskjema? {
        return namedParameterJdbcTemplate.queryForList(
                """
                    select * 
                    from permitteringsskjema_v2
                    where id = :id and opprettet_av = :opprettet_av
                """,
                mapOf("id" to id, "opprettet_av" to opprettetAv)
            ).firstOrNull()?.toPermitteringsskjema()
    }

    fun findAllByBedriftNr(bedriftNr: String): List<Permitteringsskjema> {
        return namedParameterJdbcTemplate.queryForList(
            """
                select * 
                from permitteringsskjema_v2
                where bedrift_nr = :bedrift_nr
            """,
            mapOf("bedrift_nr" to bedriftNr)
        ).map { it.toPermitteringsskjema() }
    }

    fun findAllByOpprettetAv(fnr: String): List<Permitteringsskjema> {
        return namedParameterJdbcTemplate.queryForList(
            """
                select * 
                from permitteringsskjema_v2
                where opprettet_av = :opprettet_av
            """,
            mapOf("opprettet_av" to fnr)
        ).map { it.toPermitteringsskjema()}
    }

    fun setTrukketTidspunkt(id: UUID, opprettetAv: String): Permitteringsskjema? {
        val rows = namedParameterJdbcTemplate.queryForList(
            """
        update permitteringsskjema_v2
        set trukket_tidspunkt = :tidspunkt
        where id = :id
          and opprettet_av = :opprettet_av
          and trukket_tidspunkt is null
        returning *
        """.trimIndent(),
            mapOf(
                "id" to id,
                "opprettet_av" to opprettetAv,
                "tidspunkt" to Timestamp.from(Instant.now().truncatedTo(ChronoUnit.MICROS))
            )
        )
        return rows.firstOrNull()?.toPermitteringsskjema()
    }

    fun save(skjema: Permitteringsskjema): Permitteringsskjema {
        return namedParameterJdbcTemplate.jdbcTemplate.update("""
           insert into permitteringsskjema_v2 (
                id,
                type,
                bedrift_nr,
                bedrift_navn,
                kontakt_navn,
                kontakt_epost,
                kontakt_tlf,
                antall_berort,
                arsakskode,
                yrkeskategorier,
                start_dato,
                slutt_dato,
                ukjent_slutt_dato,
                sendt_inn_tidspunkt,
                opprettet_av
           ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        ) { ps: PreparedStatement ->
            ps.setObject(1, skjema.id)
            ps.setString(2, skjema.type.name)
            ps.setString(3, skjema.bedriftNr)
            ps.setString(4, skjema.bedriftNavn)
            ps.setString(5, skjema.kontaktNavn)
            ps.setString(6, skjema.kontaktEpost)
            ps.setString(7, skjema.kontaktTlf)
            ps.setInt(8, skjema.antallBerørt)
            ps.setString(9, skjema.årsakskode.name)
            ps.setObject(10, PGobject().apply {
                type = "jsonb"
                value = objectMapper.writeValueAsString(skjema.yrkeskategorier)
            })
            ps.setDate(11, Date.valueOf(skjema.startDato))
            ps.setObject(12, skjema.sluttDato?.let { Date.valueOf(it) })
            ps.setBoolean(13, skjema.ukjentSluttDato)
            ps.setTimestamp(14, Timestamp.from(skjema.sendtInnTidspunkt))
            ps.setString(15, skjema.opprettetAv)
        }.let {
            if (it == 1) {
                skjema
            } else {
                throw RuntimeException("Kunne ikke lagre skjema")
            }
        }
    }

    private fun Map<String, Any>.toPermitteringsskjema() = Permitteringsskjema(
        id = this["id"] as UUID,
        type = SkjemaType.valueOf(this["type"] as String),
        bedriftNr = this["bedrift_nr"] as String,
        bedriftNavn = this["bedrift_navn"] as String,
        kontaktNavn = this["kontakt_navn"] as String,
        kontaktEpost = this["kontakt_epost"] as String,
        kontaktTlf = this["kontakt_tlf"] as String,
        antallBerørt = (this["antall_berort"] as BigDecimal).toInt(),
        årsakskode = Årsakskode.valueOf(this["arsakskode"] as String),
        yrkeskategorier = objectMapper.readValue((this["yrkeskategorier"] as PGobject).value!!),
        startDato = (this["start_dato"] as Date).toLocalDate(),
        sluttDato = (this["slutt_dato"] as Date?)?.toLocalDate(),
        ukjentSluttDato = this["ukjent_slutt_dato"] as Boolean,
        sendtInnTidspunkt = (this["sendt_inn_tidspunkt"] as Timestamp).toInstant(),
        trukketTidspunkt = (this["trukket_tidspunkt"] as Timestamp?)?.toInstant(),
        opprettetAv = this["opprettet_av"] as String,
    )
}


