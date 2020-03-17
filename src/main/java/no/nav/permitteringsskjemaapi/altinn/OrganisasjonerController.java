package no.nav.permitteringsskjemaapi.altinn;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.util.FnrExtractor;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organisasjoner")
@Unprotected
public class OrganisasjonerController {

    private final AltinnService altinnService;
    private FnrExtractor fnrExtractor;

    @Autowired
    public OrganisasjonerController(AltinnService altinnService, FnrExtractor fnrExtractor) {
        this.altinnService = altinnService;
        this.fnrExtractor = fnrExtractor;
    }

    @GetMapping
    public ResponseEntity<List<AltinnOrganisasjon>> hentOrganisasjoner() {
        String fnr = fnrExtractor.extract();
        List <AltinnOrganisasjon> result = altinnService.hentOrganisasjoner(fnr);
        return ResponseEntity.ok(result);
    }


}
