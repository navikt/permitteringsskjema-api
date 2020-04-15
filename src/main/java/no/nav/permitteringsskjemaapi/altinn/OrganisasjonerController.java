package no.nav.permitteringsskjemaapi.altinn;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;

@RestController
@RequestMapping("/organisasjoner")
@AllArgsConstructor
@Protected
public class OrganisasjonerController {

    private final AltinnService altinnService;
    private final TokenUtil tokenUtil;
    private final AltinnConfig altinnConfig;

    @GetMapping
    public List<AltinnOrganisasjon> hentOrganisasjoner() {
        String fnr = tokenUtil.autentisertBruker();
        return altinnService.hentOrganisasjoner(fnr);
    }

    @GetMapping("/refusjon")
    public List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter() {
        String fnr = tokenUtil.autentisertBruker();
        return altinnService.hentOrganisasjonerBasertPåRettigheter(fnr, altinnConfig.getInntektsmeldingServiceCode(), altinnConfig.getInntektsmeldingServiceEditionCode());
    }
}
