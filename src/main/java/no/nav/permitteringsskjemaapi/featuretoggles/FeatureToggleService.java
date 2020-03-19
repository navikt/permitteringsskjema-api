package no.nav.permitteringsskjemaapi.featuretoggles;


import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.UnleashContext.Builder;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeatureToggleService {

    private final Unleash unleash;
    private final TokenUtil tokenUtil;

    @Autowired
    public FeatureToggleService(Unleash unleash, TokenUtil tokenUtil) {
        this.unleash = unleash;
        this.tokenUtil = tokenUtil;
    }

    public Map<String, Boolean> hentFeatureToggles(List<String> features) {

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> isEnabled(feature)
        ));
    }

    public Boolean isEnabled(String feature) {
        return unleash.isEnabled(feature, contextMedInnloggetBruker());
    }

    private UnleashContext contextMedInnloggetBruker() {
        Builder builder = UnleashContext.builder();
        builder.userId(tokenUtil.autentisertBruker());
        return builder.build();
    }
}