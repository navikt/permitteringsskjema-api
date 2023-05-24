package no.nav.permitteringsskjemaapi.permittering

import no.nav.permitteringsskjemaapi.altinn.AltinnService
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException
import no.nav.permitteringsskjemaapi.hendelseregistrering.HendelseRegistrering
import no.nav.permitteringsskjemaapi.journalføring.JournalføringService
import no.nav.permitteringsskjemaapi.kafka.PermitteringsmeldingKafkaService
import no.nav.permitteringsskjemaapi.util.TokenUtil
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Protected
class PermitteringsskjemaController(
    private val fnrExtractor: TokenUtil,
    private val altinnService: AltinnService,
    private val repository: PermitteringsskjemaRepository,
    private val journalføringService: JournalføringService,
    private val hendelseRegistrering: HendelseRegistrering,
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/skjema")
    fun opprett(@RequestBody opprettSkjema: OpprettPermitteringsskjema): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val organisasjon = hentOrganisasjon(opprettSkjema.bedriftNr!!) ?: throw IkkeTilgangException()
        val skjema: Permitteringsskjema = Permitteringsskjema.opprettSkjema(opprettSkjema, fnr)
        skjema.bedriftNavn = organisasjon.name

        hendelseRegistrering.opprettet(skjema, fnr)
        return repository.save(skjema)
    }

    @PutMapping("/skjema/{id}")
    fun endre(@PathVariable id: UUID, @RequestBody endreSkjema: EndrePermitteringsskjema): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr).orElseThrow { IkkeFunnetException() }
        permitteringsskjema.endre(endreSkjema)

        hendelseRegistrering.endret(permitteringsskjema, fnr)
        return repository.save(permitteringsskjema)
    }

    @PostMapping("/skjema/{id}/send-inn")
    fun sendInn(@PathVariable id: UUID): Permitteringsskjema {
        val fnr = fnrExtractor.autentisertBruker()
        val permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr).orElseThrow { IkkeFunnetException() }
        permitteringsskjema.sendInn()

        hendelseRegistrering.sendtInn(permitteringsskjema, fnrExtractor.autentisertBruker())

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

        hendelseRegistrering.avbrutt(permitteringsskjema, fnrExtractor.autentisertBruker())
        return repository.save(permitteringsskjema)
    }

    fun hentOrganisasjon(bedriftNr: String) =
        altinnService.hentOrganisasjoner().firstOrNull { it.organizationNumber == bedriftNr }

    fun hentAlleSkjemaBasertPåRettighet() =
        altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1").flatMap {
            repository.findAllByBedriftNr(it.organizationNumber!!)
        }
}
