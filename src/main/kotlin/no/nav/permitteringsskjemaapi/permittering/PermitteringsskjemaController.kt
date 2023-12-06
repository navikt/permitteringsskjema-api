package no.nav.permitteringsskjemaapi.permittering

import jakarta.validation.Valid
import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.deprecated.EndrePermitteringsskjema
import no.nav.permitteringsskjemaapi.deprecated.OpprettPermitteringsskjema
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
import no.nav.permitteringsskjemaapi.util.TokenUtil
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDate
import java.util.*

@RestController
@Protected
class PermitteringsskjemaController(
    private val fnrExtractor: TokenUtil,
    private val altinnService: AltinnService,
    private val repository: PermitteringsskjemaRepository,
    private val journalføringService: JournalføringService,
    private val permitteringsmeldingKafkaService: PermitteringsmeldingKafkaService,
) {
    private val log = logger()

    @GetMapping("/skjema")
    fun hent(): List<Permitteringsskjema> {
        val fnr = fnrExtractor.autentisertBruker()

        val skjemaHentetBasertPåRettighet = hentAlleSkjemaBasertPåRettighet().toSet()
        val listeMedSkjemaBrukerenHarOpprettet = repository.findAllByOpprettetAv(fnr).toSet()

        return (skjemaHentetBasertPåRettighet + listeMedSkjemaBrukerenHarOpprettet).toList().sortedBy {
            it.sendtInnTidspunkt ?: it.opprettetTidspunkt
        }.reversed()
    }
    
    @GetMapping("/skjema/{id}")
    fun hent(@PathVariable id: UUID): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjemaOpprettetAvBruker = repository.findByIdAndOpprettetAv(id, fnr)
        if (permitteringsskjemaOpprettetAvBruker.isPresent) {
            return permitteringsskjemaOpprettetAvBruker.get()
        }
        val permitteringsskjemaOpprettetAvAnnenBruker = repository.findById(id)
        if (permitteringsskjemaOpprettetAvAnnenBruker.isPresent) {
            val orgnr = permitteringsskjemaOpprettetAvAnnenBruker.get().bedriftNr
            val organisasjonerBasertPåRettighet = altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1")
            val harRettTilÅSeSkjema = organisasjonerBasertPåRettighet.any { it.organizationNumber == orgnr }
            if (harRettTilÅSeSkjema && permitteringsskjemaOpprettetAvAnnenBruker.get().sendtInnTidspunkt != null) {
                return permitteringsskjemaOpprettetAvAnnenBruker.get()
            }
            if (!harRettTilÅSeSkjema) {
                log.warn("Bruker forsoker hente skjema uten tilgang")
            }
        }
        throw IkkeFunnetException()
    }

    @PostMapping("/skjemaV2")
    fun sendInn(@Valid @RequestBody skjema: PermitteringsskjemaDTO): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val id = UUID.randomUUID()

        /**
         * TODO:
         * - slett alle journalføring rader
         */
        journalføringService.startJournalføring(id)
        permitteringsmeldingKafkaService.scheduleSend(id)
        return repository.save(repository.save(skjema.tilDomene(id, fnr)))
    }

    // TODO: fjern gamle endepunkter under når nytt er tatt i bruk
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/skjema")
    fun opprett(@RequestBody opprettSkjema: OpprettPermitteringsskjema): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val organisasjon = hentOrganisasjon(opprettSkjema.bedriftNr!!) ?: throw IkkeTilgangException()
        val skjema: Permitteringsskjema = Permitteringsskjema.opprettSkjema(opprettSkjema, fnr)
        skjema.bedriftNavn = organisasjon.name

        return repository.save(skjema)
    }

    @PutMapping("/skjema/{id}")
    fun endre(@PathVariable id: UUID, @RequestBody endreSkjema: EndrePermitteringsskjema): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr).orElseThrow { IkkeFunnetException() }
        permitteringsskjema.endre(endreSkjema)

        return repository.save(permitteringsskjema)
    }

    @PostMapping("/skjema/{id}/send-inn")
    fun sendInn(@PathVariable id: UUID): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr).orElseThrow { IkkeFunnetException() }
        permitteringsskjema.sendInn()


        /**
         * TODO:
         * - slett alle journalføring rader
         */
        journalføringService.startJournalføring(permitteringsskjema.id!!)
        permitteringsmeldingKafkaService.scheduleSend(permitteringsskjema.id!!)
        return repository.save(permitteringsskjema)
    }

    @PostMapping("/skjema/{id}/avbryt")
    fun avbryt(@PathVariable id: UUID): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr).orElseThrow { IkkeFunnetException() }
        permitteringsskjema.avbryt()

        return repository.save(permitteringsskjema)
    }

    fun hentOrganisasjon(bedriftNr: String) =
        altinnService.hentOrganisasjoner().firstOrNull { it.organizationNumber == bedriftNr }

    fun hentAlleSkjemaBasertPåRettighet() =
        altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1").flatMap {
            repository.findAllByBedriftNr(it.organizationNumber!!)
        }
}

data class PermitteringsskjemaDTO(
    val id: UUID?,

    val type: PermitteringsskjemaType,

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
) {

    fun tilDomene(uuid: UUID, inloggetBruker: String) : Permitteringsskjema {
        return Permitteringsskjema(
            id = uuid,

            bedriftNr = bedriftNr,
            bedriftNavn = bedriftNavn,

            kontaktNavn = kontaktNavn,
            kontaktEpost = kontaktEpost,
            kontaktTlf = kontaktTlf,

            antallBerørt = antallBerørt,

            årsakskode = årsakskode,
            årsakstekst = årsakstekst,

            yrkeskategorier = yrkeskategorier.toMutableList(),

            startDato = startDato,
            sluttDato = sluttDato,
            ukjentSluttDato = ukjentSluttDato,

            fritekst = """
                ### Yrker
                ${yrkeskategorier.map { it.label }.joinToString(", ")}
                ### Årsak
                $årsakstekst
            """.trimIndent(),

            varsletAnsattDato = LocalDate.now(),
            varsletNavDato = LocalDate.now(),

            opprettetAv = inloggetBruker,
            opprettetTidspunkt = Instant.now(),
            sendtInnTidspunkt = Instant.now(),
        )
    }
}
