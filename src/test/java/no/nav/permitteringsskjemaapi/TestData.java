package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.util.UUID;

public class TestData {

    public static Permitteringsskjema enPermittering() {
        Permitteringsskjema permitteringsskjema = new Permitteringsskjema();
        permitteringsskjema.setId(UUID.randomUUID());
        permitteringsskjema.setOrgNr("999999999");
        permitteringsskjema.setOpprettetTidspunkt(Instant.now());
        return permitteringsskjema;
    }
}
