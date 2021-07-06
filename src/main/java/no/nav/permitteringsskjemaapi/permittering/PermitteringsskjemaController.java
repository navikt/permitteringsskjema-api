package no.nav.permitteringsskjemaapi.permittering;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final PermitteringsskjemaJuridiskEnhetRepository juridiskEnhetRepository;

    @GetMapping("/{id}")
    public PermitteringsskjemaJuridiskEnhet hent(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        return juridiskEnhetRepository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
    }

    @GetMapping
    public List<Permitteringsskjema> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findAllByOpprettetAv(fnr);
    }

    @PostMapping
    public ResponseEntity<PermitteringsskjemaJuridiskEnhet> opprett(@RequestBody OpprettPermitteringsskjema opprettSkjema) {
        String fnr = fnrExtractor.autentisertBruker();
        AltinnOrganisasjon organisasjon = hentOrganisasjon(fnr, opprettSkjema.getBedriftNr())
                .orElseThrow(IkkeTilgangException::new);
        PermitteringsskjemaJuridiskEnhet skjema = PermitteringsskjemaJuridiskEnhet.opprettSkjema(opprettSkjema, fnr);
        skjema.setBedriftNavn(organisasjon.getName());
        PermitteringsskjemaJuridiskEnhet lagretSkjema = juridiskEnhetRepository.save(skjema);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretSkjema);
    }

    private Optional<AltinnOrganisasjon> hentOrganisasjon(String fnr, String bedriftNr) {
        List<AltinnOrganisasjon> organisasjonerMedTilgang = altinnService.hentOrganisasjoner(fnr);
        return organisasjonerMedTilgang.stream()
                .filter(o -> o.getOrganizationNumber().equals(bedriftNr))
                .findFirst();
    }

    @PutMapping("/{id}")
    public PermitteringsskjemaJuridiskEnhet endre(@PathVariable UUID id, @RequestBody List<EndrePermitteringsskjema> endreSkjemaer) {
        String fnr = fnrExtractor.autentisertBruker();
        PermitteringsskjemaJuridiskEnhet permitteringsskjemaJuridiskEnhet = juridiskEnhetRepository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        // Loope alle skjemaer, upsert, koble til juridisk enhet
        endreSkjemaer.stream().forEach(endreSkjema -> {
            Permitteringsskjema permitteringsskjema = repository.findByIdAndOpprettetAv(permitteringsskjemaJuridiskEnhet.getId(), fnr)
                    .orElse(Permitteringsskjema.opprettFraJuridiskEnhetSkjema(permitteringsskjemaJuridiskEnhet));

            permitteringsskjema.endre(endreSkjema, fnrExtractor.autentisertBruker());
            repository.save(permitteringsskjema);
        });

        return juridiskEnhetRepository.getOne(id);
    }

    @PostMapping("/{id}/send-inn")
    public PermitteringsskjemaJuridiskEnhet sendInn(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        PermitteringsskjemaJuridiskEnhet permitteringsskjemaJuridiskEnhet = juridiskEnhetRepository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjemaJuridiskEnhet.sendInn(fnrExtractor.autentisertBruker());
        permitteringsskjemaJuridiskEnhet.getPermitteringsskjemaer().stream().forEach(skjema -> {
            skjema.sendInn(fnr);
            repository.save(skjema);
        });
        return juridiskEnhetRepository.save(permitteringsskjemaJuridiskEnhet);
    }

    @PostMapping("/{id}/avbryt")
    public PermitteringsskjemaJuridiskEnhet avbryt(@PathVariable UUID id) {
        String fnr = fnrExtractor.autentisertBruker();
        PermitteringsskjemaJuridiskEnhet permitteringsskjemaJuridiskEnhet = juridiskEnhetRepository.findByIdAndOpprettetAv(id, fnr)
                .orElseThrow(IkkeFunnetException::new);
        permitteringsskjemaJuridiskEnhet.avbryt(fnrExtractor.autentisertBruker());
        // Loope alle underskjemaer for å få event på avbrutt?
        return juridiskEnhetRepository.save(permitteringsskjemaJuridiskEnhet);
    }
}
