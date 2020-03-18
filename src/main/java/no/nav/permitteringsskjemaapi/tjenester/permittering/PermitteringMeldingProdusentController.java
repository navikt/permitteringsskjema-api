package no.nav.permitteringsskjemaapi.tjenester.permittering;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import no.nav.foreldrepenger.boot.conditionals.ConditionalOnNotProd;
import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidsgiver.Arbeidsgiver;
import no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidsgiver.ArbeidsgiverRapport;
import no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidstaker.Permittering;
import no.nav.security.token.support.core.api.Unprotected;

@RestController
@ConditionalOnNotProd
@Unprotected
public class PermitteringMeldingProdusentController {

    private final Permittering permittering;
    private final Arbeidsgiver arbeidsgiver;

    public PermitteringMeldingProdusentController(Permittering permittering, Arbeidsgiver arbeidsgiver) {
        this.permittering = permittering;
        this.arbeidsgiver = arbeidsgiver;
    }

    @PostMapping(value = "/permitter")
    public ResponseEntity<?> permitter(@RequestBody @Valid PermittertPerson person) {
        permittering.publiser(person);
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = "/rapporter")
    public ResponseEntity<?> rapporter(@RequestBody @Valid ArbeidsgiverRapport rapport) {
        arbeidsgiver.publiser(rapport);
        return ResponseEntity.ok("OK");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[permittering=" + permittering + ", arbeidsgiver=" + arbeidsgiver + "]";
    }
}
