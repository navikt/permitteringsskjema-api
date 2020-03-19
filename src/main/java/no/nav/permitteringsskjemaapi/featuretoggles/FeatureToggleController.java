package no.nav.permitteringsskjemaapi.featuretoggles;

import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Unprotected
public class FeatureToggleController {
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/feature")
    public ResponseEntity<Map<String, Boolean>> feature(@RequestParam("feature") List<String> features) {
        return ResponseEntity.status(OK).body(featureToggleService.hentFeatureToggles(features));
    }
}