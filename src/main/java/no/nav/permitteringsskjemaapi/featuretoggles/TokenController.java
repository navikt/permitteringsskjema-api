package no.nav.permitteringsskjemaapi.featuretoggles;

import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
@Profile({"dev-fss", "local"})
public class TokenController {
    private final TokenUtil tokenUtil;

    public TokenController(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @GetMapping("/token")
    public String token() {
        return tokenUtil.getTokenForInnloggetBruker();
    }
}
