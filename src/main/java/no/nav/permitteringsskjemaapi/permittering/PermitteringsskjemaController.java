package no.nav.permitteringsskjemaapi.permittering;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import no.nav.permitteringsskjemaapi.ereg.EregOrganisasjon;
import no.nav.permitteringsskjemaapi.ereg.EregService;
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
    private final EregService eregService;
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

    @PostMapping
    public ResponseEntity<Permitteringsskjema> opprett(@RequestBody OpprettPermitteringsskjema opprettSkjema) {
        String fnr = fnrExtractor.autentisertBruker();
        //AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr()).orElseThrow(IkkeFunnetException::new);
        //EregOrganisasjon brregOrg= hentOrgFraBrreg(opprettSkjema.getBedriftNr());
        //List<AltinnOrganisasjon> organisasjoner = sjekkTilgangOgHentAlleAltinnOrgs(fnr, opprettSkjema.getUnderenheter());
        Permitteringsskjema skjema = Permitteringsskjema.opprettSkjema(opprettSkjema, fnr);
        //skjema.setBedriftNavn(brregOrg.hentNavn());
        Permitteringsskjema lagretSkjema = repository.save(skjema);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }
    private List<AltinnOrganisasjon> sjekkTilgangOgHentAlleAltinnOrgs(String fnr, List<String>underenheter) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner(fnr);
       return underenheter.stream().map( o -> sjekkOmErIListeMedOrgs(o, organisasjonerMedTilgang).orElseThrow(IkkeTilgangException::new)).collect(Collectors.toList());
    }

    private Optional<AltinnOrganisasjon> sjekkOmErIListeMedOrgs(String bedriftNr,List<AltinnOrganisasjon> organisasjonerMedTilgang) {
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }
    private EregOrganisasjon hentOrgFraBrreg(String bedriftNr) {
        return eregService.hentOrganisasjon(bedriftNr);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner(fnr);
        sjekkOmErIListeMedOrgs(bedriftNr, organisasjonerMedTilgang);
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
