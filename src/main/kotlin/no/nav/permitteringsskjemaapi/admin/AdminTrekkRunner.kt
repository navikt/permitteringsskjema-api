package no.nav.permitteringsskjemaapi.admin

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.SkedulerPermitteringsmeldingService
import no.nav.permitteringsskjemaapi.permittering.HendelseType
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*
import kotlin.system.exitProcess

/**
 * One-off admin runner for å trekke skjema uten å sjekke bruker/dato.
 * Enabled kun når `admin.trekk.enabled=true` er satt.
 */
@Component
@ConditionalOnProperty(prefix = "admin.trekk", name = ["enabled"], havingValue = "true")
class AdminTrekkRunner(
    private val repository: PermitteringsskjemaRepository,
    private val journalføringService: JournalføringService,
    private val skedulerPermitteringsmeldingService: SkedulerPermitteringsmeldingService,
    @Value("\${admin.trekk.skjema-id:}") private val skjemaIdString: String,
    @Value("\${admin.trekk.performed-by:system-admin}") private val performedBy: String,
    @Value("\${admin.trekk.dry-run:false}") private val dryRun: Boolean,
) : CommandLineRunner {

    private val log = logger()

    override fun run(vararg args: String?) {
        try {
            require(skjemaIdString.isNotBlank()) { "admin.trekk.skjema-id må være satt" }
            val id = UUID.fromString(skjemaIdString)

            val existing = repository.findById(id)
                ?: error("Skjema med id=$id ikke funnet")

            if (existing.trukketTidspunkt != null) {
                log.info("Skjema {} er allerede trukket {}. Ingen videre handling.", id, existing.trukketTidspunkt)
                return
            } else {
                if (dryRun) {
                    log.info("[DRY-RUN] Ville satt trukket_tidspunkt for skjema {} til {}", id, performedBy)
                    return
                }

                val updated = repository.setTrukketTidspunkt(id, performedBy)
                    ?: error("Kunne ikke sette trukket_tidspunkt for $id")
                log.info("Satt trukket_tidspunkt for skjema {} av {} => {}", id, performedBy, updated.trukketTidspunkt)
            }

            if (dryRun) {
                log.info("[DRY-RUN] Ville køet journalføring + melding (TRUKKET) for skjema {}", id)
                return
            }

            journalføringService.startJournalføring(id, HendelseType.TRUKKET)
            skedulerPermitteringsmeldingService.scheduleSendTrukket(id)
            log.info("Køet journalføring + permitteringsmelding (TRUKKET) for skjema {}", id)

            log.info("AdminTrekkRunner vellykket fullført for skjema {}", id)
        } catch (e: Exception) {
            log.error("AdminTrekkRunner feilet: {}", e.message, e)
            exitProcess(1)
        }
        exitProcess(0)
    }
}
