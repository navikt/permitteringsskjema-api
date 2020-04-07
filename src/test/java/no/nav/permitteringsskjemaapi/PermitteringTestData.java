package no.nav.permitteringsskjemaapi;

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType;
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori;
import no.nav.permitteringsskjemaapi.permittering.Årsakskode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PermitteringTestData {

    public static Permitteringsskjema enPermitteringMedAltFyltUt() {
        var permitteringsskjema = new Permitteringsskjema();
        permitteringsskjema.setId(UUID.randomUUID());
        permitteringsskjema.setOpprettetTidspunkt(Instant.now());
        permitteringsskjema.setBedriftNr("999999999");
        permitteringsskjema.setType(PermitteringsskjemaType.PERMITTERING_UTEN_LØNN);
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
        permitteringsskjema.setÅrsakskode(Årsakskode.MANGEL_PÅ_ARBEID);

        Yrkeskategori enYrkeskategori = enYrkeskategori();
        enYrkeskategori.setPermitteringsskjema(permitteringsskjema);
        permitteringsskjema.getYrkeskategorier().add(enYrkeskategori);

        return permitteringsskjema;
    }

    public static Permitteringsskjema enPermitteringMedIkkeAltFyltUt() {
        var skjema = enPermitteringMedAltFyltUt();
        skjema.setType(null);
        return skjema;
    }

    private static Yrkeskategori enYrkeskategori() {
        var yrkeskategori = new Yrkeskategori();
        yrkeskategori.setId(UUID.randomUUID());
        yrkeskategori.setKonseptId(1000);
        yrkeskategori.setStyrk08("0001");
        yrkeskategori.setLabel("Label");
        return yrkeskategori;
    }

}
