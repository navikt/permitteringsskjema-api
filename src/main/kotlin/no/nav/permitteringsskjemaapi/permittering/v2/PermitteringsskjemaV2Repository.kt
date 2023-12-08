package no.nav.permitteringsskjemaapi.permittering.v2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Repository
class PermitteringsskjemaV2Repository(
    val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    val objectMapper: ObjectMapper,
) {
    
    fun findById(id: UUID): PermitteringsskjemaV2? {
        return namedParameterJdbcTemplate.queryForList(
                """
                    select * 
                    from permitteringsskjema_v2
                    where id = :id
                """,
                mapOf("id" to id)
            ).firstOrNull()?.toPermitteringsskjemaV2()
    }

    fun findByIdAndOpprettetAv(id: UUID, opprettetAv: String): PermitteringsskjemaV2? {
        return namedParameterJdbcTemplate.queryForList(
                """
                    select * 
                    from permitteringsskjema_v2
                    where id = :id and opprettet_av = :opprettet_av
                """,
                mapOf("id" to id, "opprettet_av" to opprettetAv)
            ).firstOrNull()?.toPermitteringsskjemaV2()
    }

    fun findAllByBedriftNr(bedriftNr: String): List<PermitteringsskjemaV2> {
        return namedParameterJdbcTemplate.queryForList(
            """
                select * 
                from permitteringsskjema_v2
                where bedrift_nr = :bedrift_nr
            """,
            mapOf("bedrift_nr" to bedriftNr)
        ).map { it.toPermitteringsskjemaV2() }
    }

    fun findAllByOpprettetAv(fnr: String): List<PermitteringsskjemaV2> {
        return namedParameterJdbcTemplate.queryForList(
            """
                select * 
                from permitteringsskjema_v2
                where opprettet_av = :opprettet_av
            """,
            mapOf("opprettet_av" to fnr)
        ).map { it.toPermitteringsskjemaV2()}
    }

    fun save(skjema: PermitteringsskjemaV2): PermitteringsskjemaV2 {
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

    private fun Map<String, Any>.toPermitteringsskjemaV2() = PermitteringsskjemaV2(
        id = this["id"] as UUID,
        type = PermitteringsskjemaType.valueOf(this["type"] as String),
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
        opprettetAv = this["opprettet_av"] as String,
    )
}

data class PermitteringsskjemaV2(
    val id: UUID,
    val type: PermitteringsskjemaType,

    val bedriftNr: String,
    val bedriftNavn: String,

    val kontaktNavn: String,
    val kontaktEpost: String,
    val kontaktTlf: String,

    val antallBerørt: Int,
    val årsakskode: Årsakskode,

    val yrkeskategorier: List<YrkeskategoriV2>,

    val startDato: LocalDate,
    val sluttDato: LocalDate?,
    val ukjentSluttDato: Boolean,

    val sendtInnTidspunkt: Instant,
    val opprettetAv: String,
) {
    val fritekst = """
        ### Yrker
        ${yrkeskategorier.joinToString(", ") { it.label }}
        ### Årsak
        ${årsakskode.navn}
    """.trimIndent()

    val årsakstekst = årsakskode.navn
}

data class YrkeskategoriV2(
    val konseptId: Int,
    val styrk08: String,
    val label: String,
) {
    // ligger igjen fra gammel modell
    // denne er eksponert i kafka melding, selv om den alltid er null
    // TODO: vurder om denne kan fjernes helt. kafka schema test er grønn uten denne
    //val antall : Int? = null
}


