package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
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
    private val permitteringsmeldingKafkaService: PermitteringsmeldingKafkaService,
) {
    private val log = logger()

    @GetMapping("/skjemaV2/{id}")
    fun hentById(@PathVariable id: UUID) : PermitteringsskjemaV2DTO? {
        val fnr = fnrExtractor.autentisertBruker()

        val permitteringsskjemaOpprettetAvBruker = repository.findByIdAndOpprettetAv(id, fnr)
        if (permitteringsskjemaOpprettetAvBruker != null) {
            return permitteringsskjemaOpprettetAvBruker.tilDTO()
        }

        val permitteringsskjemaOpprettetAvAnnenBruker = repository.findById(id)
        if (permitteringsskjemaOpprettetAvAnnenBruker != null) {
            val orgnr = permitteringsskjemaOpprettetAvAnnenBruker.bedriftNr
            val organisasjonerBasertPåRettighet = altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")
            val harRettTilÅSeSkjema = organisasjonerBasertPåRettighet.any { it == orgnr }
            if (harRettTilÅSeSkjema) {
                return permitteringsskjemaOpprettetAvAnnenBruker.tilDTO()
            } else {
                log.warn("Bruker forsoker hente skjema uten tilgang")
            }
        }
        throw IkkeFunnetException()
    }

    @GetMapping("/skjemaV2")
    fun hentAlle(): List<PermitteringsskjemaV2DTO> {
        val fnr = fnrExtractor.autentisertBruker()

        val skjemaHentetBasertPåRettighet = hentAlleSkjemaBasertPåRettighet().toSet()

        val listeMedSkjemaBrukerenHarOpprettet = repository.findAllByOpprettetAv(fnr).toSet()

        return (skjemaHentetBasertPåRettighet + listeMedSkjemaBrukerenHarOpprettet)
            .map { it.tilDTO() }
            .sortedBy { it.sendtInnTidspunkt }.reversed()
    }

    @Transactional
    @PostMapping("/skjemaV2")
    fun sendInn(@Valid @RequestBody skjema: PermitteringsskjemaV2DTO): PermitteringsskjemaV2DTO {
        val fnr = fnrExtractor.autentisertBruker()

        if (altinnService.hentOrganisasjoner().none { it.organizationNumber == skjema.bedriftNr }) {
            throw IkkeTilgangException()
        }

        /**
         * TODO:
         * - slett alle journalføring rader
         */
        val id = UUID.randomUUID()
        return repository.save(skjema.tilDomene(id, fnr)).tilDTO().also {
            journalføringService.startJournalføring(id)
            permitteringsmeldingKafkaService.scheduleSend(id)
        }
    }

    fun hentAlleSkjemaBasertPåRettighet() =
        altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1").flatMap {
            repository.findAllByBedriftNr(it)
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
) {

    fun tilDomene(uuid: UUID, inloggetBruker: String) : Permitteringsskjema {
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
        )
    }
}

private fun Permitteringsskjema.tilDTO() : PermitteringsskjemaV2DTO {
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

        sendtInnTidspunkt = sendtInnTidspunkt
    )
}
