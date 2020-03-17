package no.nav.permitteringsskjemaapi.altinn;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.util.FnrExtractor;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.ResponseEntity;
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
    private FnrExtractor fnrExtractor;


    @GetMapping
    public ResponseEntity<List<AltinnOrganisasjon>> hentOrganisasjoner() {
        String fnr = fnrExtractor.extract();
        List <AltinnOrganisasjon> result = altinnService.hentOrganisasjoner(fnr);
        return ResponseEntity.ok(result);
    }


}
