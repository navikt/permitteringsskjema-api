package no.nav.permitteringsskjemaapi.refusjon;

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
@RequestMapping("/refusjonsberegning")
@Protected
public class RefusjonssberegningController {
    private final TokenUtil fnrExtractor;
    private final AltinnService altinnService;
    private final RefusjonsberegningRepository repository;

    @GetMapping
    public List<Refusjonsberegning> hent() {
        String fnr = fnrExtractor.autentisertBruker();
        return repository.findAll();
    }
}
