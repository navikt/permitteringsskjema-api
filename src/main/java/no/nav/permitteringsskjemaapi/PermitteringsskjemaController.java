package no.nav.permitteringsskjemaapi;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon;
import no.nav.permitteringsskjemaapi.altinn.AltinnService;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException;
import no.nav.permitteringsskjemaapi.util.FnrExtractor;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/skjema")
@Protected
public class PermitteringsskjemaController {
    private final FnrExtractor fnrExtractor;
    private final AltinnService altinnService;
    private final PermitteringsskjemaRepository repository;

    @GetMapping("/{id}")
    public Permitteringsskjema hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.extract();
        return repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
    }

    @GetMapping
    public List<Permitteringsskjema> hent() {
        String fnr = fnrExtractor.extract();
        return repository.findAllByOpprettetAv(fnr);
    }

    @PostMapping
    public ResponseEntity<Permitteringsskjema> opprett(@RequestBody OpprettSkjema opprettSkjema) {
        String fnr = fnrExtractor.extract();
        AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr())
                .orElseThrow(IkkeTilgangException::new);
        Permitteringsskjema skjema = Permitteringsskjema.opprettSkjema(opprettSkjema, fnr);
        skjema.setBedriftNavn(organisasjon.getName());
        Permitteringsskjema lagretSkjema = repository.save(skjema);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner(fnr);
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }

    @PutMapping("/{id}")
    public Permitteringsskjema endre(@PathVariable UUID id, @RequestBody EndreSkjema endreSkjema) {
        String fnr = fnrExtractor.extract();
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjema.endre(endreSkjema, fnrExtractor.extract());
        return repository.save(permitteringsskjema);
    }

    @PostMapping("/{id}/send-inn")
    public Permitteringsskjema sendInn(@PathVariable UUID id) {
        String fnr = fnrExtractor.extract();
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjema.sendInn(fnrExtractor.extract());
        return repository.save(permitteringsskjema);
    }
}
