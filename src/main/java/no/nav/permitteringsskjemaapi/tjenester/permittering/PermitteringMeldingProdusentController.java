package no.nav.permitteringsskjemaapi.tjenester.permittering;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import no.nav.foreldrepenger.boot.conditionals.ConditionalOnNotProd;
import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.security.token.support.core.api.Unprotected;

@RestController
@ConditionalOnNotProd
@Unprotected
public class PermitteringMeldingProdusentController {

    private final Permittering permittering;

    public PermitteringMeldingProdusentController(Permittering permittering) {
        this.permittering = permittering;
    }

    @PostMapping(value = "/permitter")
    public ResponseEntity<?> permitter(@RequestBody @Valid PermittertPerson person) {
        permittering.publiser(person);
        return ResponseEntity.ok("OK");
    }
}
