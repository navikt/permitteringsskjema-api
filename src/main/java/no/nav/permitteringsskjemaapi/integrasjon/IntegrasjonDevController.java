package no.nav.permitteringsskjemaapi.integrasjon;

import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.ArbeidsgiverRapport;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.PermitteringsskjemaProdusent;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static no.nav.permitteringsskjemaapi.config.Constants.*;

@RestController
@Unprotected
@Profile({LOCAL, DEV_GCP})
public class IntegrasjonDevController {

    private final PermitteringsskjemaProdusent permitteringsskjemaProdusent;

    public IntegrasjonDevController(
            PermitteringsskjemaProdusent permitteringsskjemaProdusent
    ) {
        this.permitteringsskjemaProdusent = permitteringsskjemaProdusent;
    }

    @PostMapping(value = "/rapporter")
    public ResponseEntity<?> rapporter(@RequestBody @Valid ArbeidsgiverRapport rapport) {
        permitteringsskjemaProdusent.publiser(rapport);
        return ResponseEntity.ok("OK");
    }
}
