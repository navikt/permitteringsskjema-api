package no.nav.permitteringsskjemaapi.altinn;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.nav.security.token.support.core.api.Protected;

@RestController
@RequestMapping("/organisasjoner")
@AllArgsConstructor
@Protected
public class OrganisasjonerController {

    private final AltinnService altinnService;

    @GetMapping
    public List<AltinnOrganisasjon> hentOrganisasjoner() {
        return altinnService.hentOrganisasjoner();
    }
}
