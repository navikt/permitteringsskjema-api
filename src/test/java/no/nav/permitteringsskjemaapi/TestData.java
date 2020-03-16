package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TestData {

    public static Permitteringsskjema enPermittering() {
        Permitteringsskjema permitteringsskjema = new Permitteringsskjema();
        permitteringsskjema.setId(UUID.randomUUID());
        permitteringsskjema.setOpprettetTidspunkt(Instant.now());
        permitteringsskjema.setOrgNr("999999999");
        permitteringsskjema.setType(SkjemaType.PERMITTERING_UTEN_LØNN);
        permitteringsskjema.setKontaktNavn("Tore Toresen");
        permitteringsskjema.setKontaktTlf("66778899");
        permitteringsskjema.setVarsletAnsattDato(LocalDate.of(2020, 3, 16));
        permitteringsskjema.setVarsletNavDato(LocalDate.of(2020, 9, 21));
        permitteringsskjema.setStartDato(LocalDate.of(2020, 3, 17));
        permitteringsskjema.setSluttDato(LocalDate.of(2020, 9, 18));
        permitteringsskjema.setUkjentSluttDato(false);
        permitteringsskjema.setFritekst("Fritekst");
        Person enPerson = enPerson();
        enPerson.setPermitteringsskjema(permitteringsskjema);
        permitteringsskjema.getPersoner().add(enPerson);
        return permitteringsskjema;
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
