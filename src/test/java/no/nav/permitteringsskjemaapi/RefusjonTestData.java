package no.nav.permitteringsskjemaapi;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class RefusjonTestData {
    public static Refusjonsskjema enRefusjonMedAltFyltUt() {
        Refusjonsskjema refusjonsskjema = new Refusjonsskjema();
        refusjonsskjema.setId(UUID.randomUUID());
        refusjonsskjema.setOpprettetTidspunkt(Instant.now());
        refusjonsskjema.setOpprettetAv("00000000000");
        refusjonsskjema.setBedriftNr("999999999");
        refusjonsskjema.setBedriftNavn("999999999");
        return refusjonsskjema;
    }
}
