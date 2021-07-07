package no.nav.permitteringsskjemaapi.permittering;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Value
// Value object med felter som kan endres på et skjema
public class EndrePermitteringsskjema {
    PermitteringsskjemaType type;
    String kontaktNavn;
    String kontaktTlf;
    String kontaktEpost;
    LocalDate varsletAnsattDato;
    LocalDate varsletNavDato;
    LocalDate startDato;
    LocalDate sluttDato;
    Boolean ukjentSluttDato;
    String fritekst;
    Integer antallBerørt;
    List<Yrkeskategori> yrkeskategorier = new ArrayList<>();
    Årsakskode årsakskode;
    String årsakstekst;
}
