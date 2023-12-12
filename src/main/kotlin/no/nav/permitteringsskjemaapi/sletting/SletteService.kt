package no.nav.permitteringsskjemaapi.sletting

import no.nav.permitteringsskjemaapi.config.logger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SletteService(
    val jdbcTemplate: JdbcTemplate,
) {

    private val log = logger()

    @Scheduled(
        initialDelayString = "PT1M",
        fixedDelayString = "PT12H",
    )
    fun slettGammelData() {
        val oldestTimestamp = Instant.now().minus(365 * 2, ChronoUnit.DAYS)
        jdbcTemplate.update(
            """
                delete from permitteringsskjema_v2
                where sendt_inn_tidspunkt < ?
            """) { ps ->
            ps.setTimestamp(1, java.sql.Timestamp.from(oldestTimestamp))
        }.let { deleted ->
            if (deleted > 0) {
                log.info("Slettet $deleted gamle rader fra permitteringsskjema_v2")
            }
        }

        jdbcTemplate.update(
            """
                delete from journalforing
                where journalforing.journalfort_at < ?
            """) { ps ->
            ps.setTimestamp(1, java.sql.Timestamp.from(oldestTimestamp))
        }.let { deleted ->
            if (deleted > 0) {
                log.info("Slettet $deleted gamle rader fra journalforing")
            }
        }


        // delete from legacy tables. remove this and drop the table when all data is gone
        // note: all data has been migrated to permitteringsskjema_v2, but is kept for histoic value in case of bugs
        jdbcTemplate.update(
            """
                with deleted as (
                    delete from permitteringsskjema
                    where sendt_inn_tidspunkt < ?
                    returning id
                )
                delete from yrkeskategori where permitteringsskjema_id in (select id from deleted);
            """) { ps ->
            ps.setTimestamp(1, java.sql.Timestamp.from(oldestTimestamp))
        }.let { deleted ->
            if (deleted > 0) {
                log.info("Slettet $deleted gamle rader fra permitteringsskjema med tilhørende yrkeskategorier")
            }
        }
    }
}