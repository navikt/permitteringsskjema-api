package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.AlleredeTrukketException
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException
import no.nav.permitteringsskjemaapi.exceptions.StartdatoPassertException
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.SkedulerPermitteringsmeldingService
import no.nav.permitteringsskjemaapi.util.AuthenticatedUserHolder
import no.nav.security.token.support.core.api.Protected
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*


@RestController
@Protected
class PermitteringsskjemaController(
    private val fnrExtractor: AuthenticatedUserHolder,
    private val altinnService: AltinnService,
    private val repository: PermitteringsskjemaRepository,
    private val journalføringService: JournalføringService,
    private val skedulerPermitteringsmeldingService: SkedulerPermitteringsmeldingService,
) {

    private val log = logger()

    @GetMapping("/skjemaV2/{id}")
    fun hentById(@PathVariable id: UUID): PermitteringsskjemaV2DTO? {
        val permitteringsSkjema = repository.findById(id)
        if (permitteringsSkjema != null) {
            val orgnr = permitteringsSkjema.bedriftNr
            val harInnsynIVirksomhet = altinnService.harTilgang(orgnr, INNSYN_ALLE_PERMITTERINGSSKJEMA)
            if (harInnsynIVirksomhet) {
                return permitteringsSkjema.tilDTO()
            } else {
                log.warn("Bruker forsoker hente skjema uten tilgang")
            }
        }
        throw IkkeFunnetException()
    }

    @Transactional
    @PostMapping("/skjemaV2/{id}/trekk")
    fun trekk(@PathVariable id: UUID): PermitteringsskjemaV2DTO {
        val fnr = fnrExtractor.autentisertBruker()

        val skjema = repository.findById(id) ?: throw IkkeFunnetException()

        val kanTrekke = altinnService.hentAlleOrgnr(INNSYN_ALLE_PERMITTERINGSSKJEMA).any { it == skjema.bedriftNr }

        if (!kanTrekke) {
            throw IkkeTilgangException()
        }

        val today = LocalDate.now()
        // Trekk er kun mulig frem til dagen før startdato (23:59:59)
        if (skjema.startDato <= today) {
            throw StartdatoPassertException()
        }

        if (skjema.trukketTidspunkt != null) {
            throw AlleredeTrukketException()
        }

        val oppdatertSkjema = repository.setTrukketTidspunkt(id, fnr) ?: throw IkkeFunnetException()

        return oppdatertSkjema.tilDTO().also {
            journalføringService.startJournalføring(id, HendelseType.TRUKKET)
            skedulerPermitteringsmeldingService.scheduleSendTrukket(id)
        }
    }

    @GetMapping("/skjemaV2")
    fun hentAlle(): List<PermitteringsskjemaV2DTO> {
        val alleOrgnrMedInnsynTilgang = altinnService.hentAlleOrgnr(INNSYN_ALLE_PERMITTERINGSSKJEMA)
        val skjemaer = alleOrgnrMedInnsynTilgang.flatMap {
            repository.findAllByBedriftNr(it)
        }.toSet()

        return (skjemaer)
            .map { it.tilDTO() }
            .sortedBy { it.sendtInnTidspunkt }.reversed()
    }

    @Transactional
    @PostMapping("/skjemaV2")
    fun sendInn(@Valid @RequestBody skjema: PermitteringsskjemaV2DTO): PermitteringsskjemaV2DTO {
        val fnr = fnrExtractor.autentisertBruker()

        val kanSendeInn = altinnService.hentAlleOrgnr(INNSYN_ALLE_PERMITTERINGSSKJEMA).any { it == skjema.bedriftNr }
        if (!kanSendeInn) {
            throw IkkeTilgangException()
        }

        /**
         * TODO:
         * - slett alle journalføring rader
         */
        val id = UUID.randomUUID()
        return repository.save(skjema.tilDomene(id, fnr)).tilDTO().also {
            journalføringService.startJournalføring(id, HendelseType.INNSENDT)
            skedulerPermitteringsmeldingService.scheduleSendInnsendt(id)
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PermitteringsskjemaV2DTO(
    val id: UUID?,

    val type: SkjemaType,

    val bedriftNr: String,
    val bedriftNavn: String,

    val kontaktNavn: String,
    val kontaktEpost: String,
    val kontaktTlf: String,

    val antallBerørt: Int,

    val årsakskode: Årsakskode,
    val årsakstekst: String,

    val yrkeskategorier: List<Yrkeskategori>,

    val startDato: LocalDate,
    val sluttDato: LocalDate?,
    val ukjentSluttDato: Boolean = false,

    val sendtInnTidspunkt: Instant?,
    val trukketTidspunkt: Instant? = null,
) {

    fun tilDomene(uuid: UUID, inloggetBruker: String): Permitteringsskjema {
        return Permitteringsskjema(
            id = uuid,
            type = type,

            bedriftNr = bedriftNr,
            bedriftNavn = bedriftNavn,

            kontaktNavn = kontaktNavn,
            kontaktEpost = kontaktEpost,
            kontaktTlf = kontaktTlf,

            antallBerørt = antallBerørt,

            årsakskode = årsakskode,

            startDato = startDato,
            sluttDato = sluttDato,
            ukjentSluttDato = ukjentSluttDato,

            yrkeskategorier = yrkeskategorier,
            opprettetAv = inloggetBruker,
            sendtInnTidspunkt = Instant.now().truncatedTo(ChronoUnit.MICROS),
            trukketTidspunkt = trukketTidspunkt,
        )
    }
}

private fun Permitteringsskjema.tilDTO(): PermitteringsskjemaV2DTO {
    return PermitteringsskjemaV2DTO(
        id = id,
        type = type,

        bedriftNr = bedriftNr,
        bedriftNavn = bedriftNavn,

        kontaktNavn = kontaktNavn,
        kontaktEpost = kontaktEpost,
        kontaktTlf = kontaktTlf,

        antallBerørt = antallBerørt,

        årsakskode = årsakskode,

        årsakstekst = årsakskode.navn,

        yrkeskategorier = yrkeskategorier,

        startDato = startDato,
        sluttDato = sluttDato,
        ukjentSluttDato = ukjentSluttDato,

        sendtInnTidspunkt = sendtInnTidspunkt,
        trukketTidspunkt = trukketTidspunkt,
    )
}
