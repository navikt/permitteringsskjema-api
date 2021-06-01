package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.permitteringsskjemaapi.config.Constants.*;

@RestController
@Unprotected
@Profile({LOCAL, DEV_FSS})
public class DevConsumerController {

}
