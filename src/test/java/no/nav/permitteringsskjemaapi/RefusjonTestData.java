package no.nav.permitteringsskjemaapi;

import lombok.experimental.UtilityClass;
import no.nav.permitteringsskjemaapi.refusjon.Arbeidsforhold;
import no.nav.permitteringsskjemaapi.refusjon.Refusjonsskjema;

import java.time.Instant;
import java.time.LocalDate;
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
        refusjonsskjema.setKontaktNavn("Navn");
        refusjonsskjema.setKontaktTlf("22222222");
        refusjonsskjema.setKontaktEpost("navn@server.com");

        return refusjonsskjema;
    }

    private static Arbeidsforhold etArbeidsforhold() {
        var arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.setId(UUID.randomUUID());
        arbeidsforhold.setFnr("00000000000");
        arbeidsforhold.setGradering(50);
        arbeidsforhold.setPeriodeStart(LocalDate.now().minusWeeks(2));
        arbeidsforhold.setPeriodeSlutt(LocalDate.now().minusWeeks(1));
        return arbeidsforhold;
    }
}
