package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TestData {

    public static Permitteringsskjema enPermitteringMedAltFyltUt() {
        Permitteringsskjema permitteringsskjema = new Permitteringsskjema();
        permitteringsskjema.setId(UUID.randomUUID());
        permitteringsskjema.setOpprettetTidspunkt(Instant.now());
        permitteringsskjema.setBedriftNr("999999999");
        permitteringsskjema.setType(SkjemaType.PERMITTERING_UTEN_LØNN);
        permitteringsskjema.setKontaktNavn("Tore Toresen");
        permitteringsskjema.setKontaktTlf("66778899");
        permitteringsskjema.setKontaktEpost("per@bedrift.no");
        permitteringsskjema.setVarsletAnsattDato(LocalDate.of(2020, 3, 16));
        permitteringsskjema.setVarsletNavDato(LocalDate.of(2020, 9, 21));
        permitteringsskjema.setStartDato(LocalDate.of(2020, 3, 17));
        permitteringsskjema.setSluttDato(LocalDate.of(2020, 9, 18));
        permitteringsskjema.setUkjentSluttDato(false);
        permitteringsskjema.setFritekst("Fritekst");
        permitteringsskjema.setAntallBerørt(1);
        //permitteringsskjema.setÅrsakskode(Årsakskode.MANGEL_PÅ_ARBEID);
        Person enPerson = enPerson();
        enPerson.setPermitteringsskjema(permitteringsskjema);
        permitteringsskjema.getPersoner().add(enPerson);
        return permitteringsskjema;
    }

    public static Permitteringsskjema enPermitteringMedIkkeAltFyltUt() {
        var skjema = enPermitteringMedAltFyltUt();
        skjema.setType(null);
        return skjema;
    }

    private static Person enPerson() {
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setFnr("00000000000");
        person.setGrad(100);
        person.setKommentar("Kommentar");
        return person;
    }

}
