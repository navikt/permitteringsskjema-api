package no.nav.permitteringsskjemaapi.refusjon;

import java.math.BigDecimal;

public interface InntektskomponentClient {
    // Både response og request må utvides
    BigDecimal hentInntekt();
}
