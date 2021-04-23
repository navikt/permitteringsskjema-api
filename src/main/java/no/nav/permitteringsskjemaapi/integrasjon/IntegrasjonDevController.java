package no.nav.permitteringsskjemaapi.integrasjon;

import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.Arbeidsgiver;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.ArbeidsgiverRapport;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static no.nav.permitteringsskjemaapi.config.Constants.DEV_FSS;
import static no.nav.permitteringsskjemaapi.config.Constants.LOCAL;
import static no.nav.permitteringsskjemaapi.config.Constants.DEFAULT;

@RestController
@Unprotected
@Profile({DEFAULT, LOCAL, DEV_FSS})
public class IntegrasjonDevController {

    private final Arbeidsgiver arbeidsgiver;

    public IntegrasjonDevController(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @PostMapping(value = "/rapporter")
    public ResponseEntity<?> rapporter(@RequestBody @Valid ArbeidsgiverRapport rapport) {
        arbeidsgiver.publiser(rapport);
        return ResponseEntity.ok("OK");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[arbeidsgiver=" + arbeidsgiver + "]";
    }
}
