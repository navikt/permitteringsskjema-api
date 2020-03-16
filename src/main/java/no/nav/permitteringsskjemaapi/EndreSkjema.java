package no.nav.permitteringsskjemaapi;

import lombok.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Value
// Value object med felter som kan endres på et skjema
public class EndreSkjema {
    SkjemaType type;
    String kontaktNavn;
    String kontaktTlf;
    LocalDate varsletAnsattDato;
    LocalDate varsletNavDato;
    LocalDate startDato;
    LocalDate sluttDato;
    Boolean ukjentSluttDato;
    String fritekst;
    List<Person> personer = new ArrayList<>();
}
