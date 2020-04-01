package no.nav.permitteringsskjemaapi;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon;
import no.nav.permitteringsskjemaapi.altinn.AltinnService;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/refusjon")
@Protected
public class RefusjonController {
    private final TokenUtil fnrExtractor;
    private final AltinnService altinnService;
    private final RefusjonRepository repository;

    @GetMapping("/{id}")
    public Refusjon hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
    }

    @GetMapping
    public List<Refusjon> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findAllByOpprettetAv(fnr);
    }

    @PostMapping
    public ResponseEntity<Refusjon> opprett(@RequestBody OpprettRefusjon opprettSkjema) {
        String fnr = fnrExtractor.autentisertBruker();
        AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr())
                .orElseThrow(IkkeTilgangException::new);
        Refusjon refusjon = Refusjon.opprett(opprettSkjema, fnr);
        refusjon.setBedriftNavn(organisasjon.getName());
        Refusjon lagretSkjema = repository.save(refusjon);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner(fnr);
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }

    @PutMapping("/{id}")
    public Refusjon endre(@PathVariable UUID id, @RequestBody EndreRefusjon endre) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjon refusjon = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjon.endre(endre, fnrExtractor.autentisertBruker());
        return repository.save(refusjon);
    }

    @PostMapping("/{id}/send-inn")
    public Refusjon sendInn(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjon refusjon = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjon.sendInn(fnrExtractor.autentisertBruker());
        return repository.save(refusjon);
    }

    @PostMapping("/{id}/avbryt")
    public Refusjon avbryt(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjon refusjon = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjon.avbryt(fnrExtractor.autentisertBruker());
        return repository.save(refusjon);
    }
}
