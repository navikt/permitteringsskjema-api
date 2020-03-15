package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.util.UUID;

public class TestData {

    public static Permittering enPermittering() {
        Permittering permittering = new Permittering();
        permittering.setId(UUID.randomUUID());
        permittering.setBedriftNr("999999999");
        permittering.setOpprettetTidspunkt(Instant.now());
        return permittering;
    }
}
