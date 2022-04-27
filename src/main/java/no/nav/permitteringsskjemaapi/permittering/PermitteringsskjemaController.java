package no.nav.permitteringsskjemaapi.permittering;

import java.util.*;
import java.util.stream.Collectors;

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
public class PermitteringsskjemaController {
    private final TokenUtil fnrExtractor;
    private final AltinnService altinnService;
    private final PermitteringsskjemaRepository repository;

    @GetMapping("/{id}")
    public Permitteringsskjema hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
    }

    @GetMapping
    public List<Permitteringsskjema> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findAllByOpprettetAv(fnr);
    }

    @GetMapping
    public List<Permitteringsskjema> hentAlleSkjemaBasertPåRettighet(String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerBasertPåRettighet = altinnService.hentOrganisasjonerBasertPåRettigheter(" ", "");
        List<AltinnOrganisasjon> orgs = organisasjonerBasertPåRettighet.stream().filter(organisasjon -> organisasjon.getOrganizationNumber().equals( bedriftNr)).collect(Collectors.toList());
        //if (orgs.size() > 0) {
        //}
        List<Permitteringsskjema> liste = new ArrayList<>(Collections.emptyList());
        orgs.forEach(org -> {
            List<Permitteringsskjema> lizt = repository.findAllByOrganisasjon(org.getOrganizationNumber()  );
            liste.addAll(lizt);
        });

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
