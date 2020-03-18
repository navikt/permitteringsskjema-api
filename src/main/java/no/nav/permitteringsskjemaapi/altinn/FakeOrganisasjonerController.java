package no.nav.permitteringsskjemaapi.altinn;

import lombok.AllArgsConstructor;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organisasjoner")
@AllArgsConstructor
@Unprotected
@ConditionalOnLocal
public class FakeOrganisasjonerController {

    @GetMapping
    public List<AltinnOrganisasjon> hentOrganisasjoner() {
        return List.of(new AltinnOrganisasjon("STORFOSNA OG FREDRIKSTAD REGNSKAP", "Business", "910825569", "BEDR", "Active", "910825550"));
    }

}
