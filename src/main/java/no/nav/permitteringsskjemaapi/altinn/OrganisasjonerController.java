package no.nav.permitteringsskjemaapi.altinn;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.util.FnrExtractor;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organisasjoner")
@AllArgsConstructor
@Protected
public class OrganisasjonerController {

    private final AltinnService altinnService;
    private final FnrExtractor fnrExtractor;

    @GetMapping
    public List<AltinnOrganisasjon> hentOrganisasjoner() {
        String fnr = fnrExtractor.extract();
        return altinnService.hentOrganisasjoner(fnr);
    }
}
