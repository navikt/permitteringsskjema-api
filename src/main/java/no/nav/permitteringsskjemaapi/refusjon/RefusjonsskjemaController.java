package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.altinn.AltinnConfig;
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon;
import no.nav.permitteringsskjemaapi.altinn.AltinnService;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import no.nav.permitteringsskjemaapi.exceptions.IkkeTilgangException;
import no.nav.permitteringsskjemaapi.featuretoggles.FeatureToggleService;
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
public class RefusjonsskjemaController {
    private final TokenUtil fnrExtractor;
    private final AltinnService altinnService;
    private final RefusjonsskjemaRepository repository;
    private final FeatureToggleService featureToggleService;
    private final AltinnConfig altinnConfig;

    @GetMapping("/{id}")
    public Refusjonsskjema hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
    }

    @GetMapping
    public List<Refusjonsskjema> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findAllByOpprettetAv(fnr);
    }

    @PostMapping
    public ResponseEntity<Refusjonsskjema> opprett(@RequestBody OpprettRefusjonsskjema opprettSkjema) {
        if (!featureToggleService.isEnabled("arbeidsgiver.permitteringsskjema-api.refusjon")) {
            throw new RuntimeException("Ikke lansert");
        }
        String fnr = fnrExtractor.autentisertBruker();
        AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr())
                .orElseThrow(IkkeTilgangException::new);
        Refusjonsskjema refusjonsskjema = Refusjonsskjema.opprett(opprettSkjema, fnr);
        refusjonsskjema.setBedriftNavn(organisasjon.getName());
        Refusjonsskjema lagretSkjema = repository.save(refusjonsskjema);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjonerBasertPaRettigheter(fnr, altinnConfig.getInntektsmeldingServieCode(), altinnConfig.getInntektsmeldingServiceEditionCode());
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }

    @PutMapping("/{id}")
    public Refusjonsskjema endre(@PathVariable UUID id, @RequestBody EndreRefusjonsskjema endre) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjonsskjema refusjonsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjonsskjema.endre(endre, fnrExtractor.autentisertBruker());
        return repository.save(refusjonsskjema);
    }

    @PostMapping("/{id}/send-inn")
    public Refusjonsskjema sendInn(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjonsskjema refusjonsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjonsskjema.sendInn(fnrExtractor.autentisertBruker());
        return repository.save(refusjonsskjema);
    }

    @PostMapping("/{id}/avbryt")
    public Refusjonsskjema avbryt(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        Refusjonsskjema refusjonsskjema = repository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        refusjonsskjema.avbryt(fnrExtractor.autentisertBruker());
        return repository.save(refusjonsskjema);
    }
}
