package no.nav.permitteringsskjemaapi.permittering;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon;
import no.nav.permitteringsskjemaapi.altinn.AltinnService;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;

@RestController
@AllArgsConstructor
@RequestMapping("/skjema")
@Protected
@Slf4j
public class PermitteringsskjemaController {
    private final TokenUtil fnrExtractor;
    private final AltinnService altinnService;
    private final PermitteringsskjemaRepository repository;

    @GetMapping("/{id}")
    public Permitteringsskjema hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Optional<Permitteringsskjema> permitteringsskjemaOpprettetAvBruker = repository.findByIdAndOpprettetAv(id, fnr);
        if (permitteringsskjemaOpprettetAvBruker.isPresent()) {
            return permitteringsskjemaOpprettetAvBruker.get();
        }
        Optional<Permitteringsskjema> permitteringsskjemaOpprettetAvAnnenBruker = repository.findById(id);
        if (permitteringsskjemaOpprettetAvAnnenBruker.isPresent()) {
            String orgnr = permitteringsskjemaOpprettetAvAnnenBruker.get().getBedriftNr();
            List<AltinnOrganisasjon> organisasjonerBasertPåRettighet = altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1");
            Boolean harRettTilÅSeSkjema = organisasjonerBasertPåRettighet.stream().anyMatch(organisasjon -> organisasjon.getOrganizationNumber().equals(orgnr));
            if (harRettTilÅSeSkjema && permitteringsskjemaOpprettetAvAnnenBruker.get().getSendtInnTidspunkt() !=null ) {
                return permitteringsskjemaOpprettetAvAnnenBruker.get();
            }
        }
        throw new IkkeFunnetException();
    }

    @GetMapping
    public List<Permitteringsskjema> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        List<Permitteringsskjema> alleSkjema = new ArrayList<>(Collections.emptyList());
        List<Permitteringsskjema> skjemaHentetBasertPåRettighet = hentAlleSkjemaBasertPåRettighet();
        List<Permitteringsskjema> listeMedSkjemaBrukerenHarOpprettet = repository.findAllByOpprettetAv(fnr);
        if (skjemaHentetBasertPåRettighet.size() > 0) {
            alleSkjema.addAll(skjemaHentetBasertPåRettighet);
        }
        else {
            return listeMedSkjemaBrukerenHarOpprettet;
        }
        if (listeMedSkjemaBrukerenHarOpprettet.size() >0 ) {
            AtomicReference<Boolean> skjemaAlleredeLagtTil = new AtomicReference<>(false);
            listeMedSkjemaBrukerenHarOpprettet.forEach(skjemaBrukerenHarOpprettet -> {
                alleSkjema.forEach( skjema -> {
                    if (skjema.getId().equals(skjemaBrukerenHarOpprettet.getId()) && !skjemaAlleredeLagtTil.get()) {
                        log.info("bruker har tilgang til skjema basert på rettighet");
                        skjemaAlleredeLagtTil.set(true);
                    }
                });
                if (!skjemaAlleredeLagtTil.get()) {
                    log.info("bruker har ikke altinnrettighet, men har opprettet skjemaet");
                    alleSkjema.add(skjemaBrukerenHarOpprettet);
                }
            });
        }
        return alleSkjema;

    }

    public List<Permitteringsskjema> hentAlleSkjemaBasertPåRettighet() {
        List<AltinnOrganisasjon> organisasjonerBasertPåRettighet = altinnService.hentOrganisasjonerBasertPåRettigheter("5810", "1");
        log.info("Forsøker hente organisasjoner basert på rettigheter, {}", organisasjonerBasertPåRettighet.size());
        List<Permitteringsskjema> liste = new ArrayList<>(Collections.emptyList());
        if (organisasjonerBasertPåRettighet.size() > 0) {
            log.info("Bruker har tilgang på organisasjoner basert på rettigheter, {}", organisasjonerBasertPåRettighet.size());
            organisasjonerBasertPåRettighet.forEach(org -> {
                List<Permitteringsskjema> listeMedInnsendteSkjema = repository.findAllByBedriftNr(org.getOrganizationNumber()).stream().filter(skjema -> skjema.getSendtInnTidspunkt() != null).collect(Collectors.toList());
                liste.addAll(listeMedInnsendteSkjema);
            });
        }
        return liste;
    }

    @PostMapping
    public ResponseEntity<Permitteringsskjema> opprett(@RequestBody OpprettPermitteringsskjema opprettSkjema) {
        String fnr = fnrExtractor.autentisertBruker();
        AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr())
                .orElseThrow(IkkeTilgangException::new);
        Permitteringsskjema skjema = Permitteringsskjema.opprettSkjema(opprettSkjema, fnr);
        skjema.setBedriftNavn(organisasjon.getName());
        Permitteringsskjema lagretSkjema = repository.save(skjema);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner();
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }

    @PutMapping("/{id}")
    public Permitteringsskjema endre(@PathVariable UUID id, @RequestBody EndrePermitteringsskjema endreSkjema) {
        String fnr = fnrExtractor.autentisertBruker();
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjema.endre(endreSkjema, fnrExtractor.autentisertBruker());
        return repository.save(permitteringsskjema);
    }

    @PostMapping("/{id}/send-inn")
    public Permitteringsskjema sendInn(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjema.sendInn(fnrExtractor.autentisertBruker());
        return repository.save(permitteringsskjema);
    }

    @PostMapping("/{id}/avbryt")
    public Permitteringsskjema avbryt(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjema.avbryt(fnrExtractor.autentisertBruker());
        return repository.save(permitteringsskjema);
    }
}
