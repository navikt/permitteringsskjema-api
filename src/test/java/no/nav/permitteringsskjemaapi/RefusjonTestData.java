package no.nav.permitteringsskjemaapi;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class RefusjonTestData {
    public static Refusjon enRefusjonMedAltFyltUt() {
        Refusjon refusjon = new Refusjon();
        refusjon.setId(UUID.randomUUID());
        refusjon.setOpprettetTidspunkt(Instant.now());
        refusjon.setOpprettetAv("00000000000");
        refusjon.setBedriftNr("999999999");
        refusjon.setBedriftNavn("999999999");
        return refusjon;
    }
}
