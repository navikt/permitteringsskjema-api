package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException
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
        val fnr = fnrExtractor.autentisertBruker()

        val permitteringsskjemaOpprettetAvBruker = repository.findByIdAndOpprettetAv(id, fnr)
        if (permitteringsskjemaOpprettetAvBruker != null) {
            return permitteringsskjemaOpprettetAvBruker.tilDTO()
        }

        val permitteringsskjemaOpprettetAvAnnenBruker = repository.findById(id)
        if (permitteringsskjemaOpprettetAvAnnenBruker != null) {
            val orgnr = permitteringsskjemaOpprettetAvAnnenBruker.bedriftNr
            val harInnsynIVirksomhet = altinnService.harTilgang(orgnr, INNSYN_ALLE_PERMITTERINGSSKJEMA)
            if (harInnsynIVirksomhet) {
                return permitteringsskjemaOpprettetAvAnnenBruker.tilDTO()
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

        // Legge til permitteringsskjemaOpprettetAvAnnenBruker
        val skjema = repository.findByIdAndOpprettetAv(id, fnr) ?: throw IkkeTilgangException()

        if (!skjema.ukjentSluttDato) {
            val today = LocalDate.now()
            if (skjema.startDato <= today) {
                throw IllegalStateException("Skjema kan ikke trekkes etter startdato")
            }
        }

        if (skjema.trukketTidspunkt != null) {
            throw IllegalStateException("Skjema er allerede trukket")
        }

        val oppdatertSkjema = repository.setTrukketTidspunkt(id, fnr) ?: throw IkkeFunnetException()

        skedulerPermitteringsmeldingService.scheduleSendTrukket(id)

        // Opprette beskjed på sak når permittering/masseoppsigelse/innskrenking er trukket - ikke ekstern varsling
        // Journalfør i Joark at skjemaet er trukket – opprett ny journalpost på saken og ferdigstill den.
        // Send ny melding på kafka med skjemaId og trukketTidspunkt
        return oppdatertSkjema.tilDTO()
    }

    @GetMapping("/skjemaV2")
    fun hentAlle(): List<PermitteringsskjemaV2DTO> {
        val fnr = fnrExtractor.autentisertBruker()

        val alleOrgnrMedInnsynTilgang = altinnService.hentAlleOrgnr(INNSYN_ALLE_PERMITTERINGSSKJEMA)
        val skjemaHentetBasertPåInnsynTilgang = alleOrgnrMedInnsynTilgang.flatMap {
            repository.findAllByBedriftNr(it)
        }.toSet()

        val skjemaBrukerenHarOpprettet = repository.findAllByOpprettetAv(fnr).toSet()

        return (skjemaHentetBasertPåInnsynTilgang + skjemaBrukerenHarOpprettet)
            .map { it.tilDTO() }
            .sortedBy { it.sendtInnTidspunkt }.reversed()
    }

    @Transactional
    @PostMapping("/skjemaV2")
    fun sendInn(@Valid @RequestBody skjema: PermitteringsskjemaV2DTO): PermitteringsskjemaV2DTO {
        val fnr = fnrExtractor.autentisertBruker()

        // Her kommer det på sikt en ny ressursid vi skal sjekke mot
        val kanSendeInn = altinnService.hentAlleOrgnr().any { it == skjema.bedriftNr }
        if (!kanSendeInn) {
            throw IkkeTilgangException()
        }

        /**
         * TODO:
         * - slett alle journalføring rader
         */
        val id = UUID.randomUUID()
        return repository.save(skjema.tilDomene(id, fnr)).tilDTO().also {
            journalføringService.startJournalføring(id)
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
