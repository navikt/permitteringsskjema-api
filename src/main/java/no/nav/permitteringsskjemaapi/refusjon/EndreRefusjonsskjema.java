package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class EndreRefusjonsskjema {
    String kontaktEpost;
    String kontaktNavn;
    String kontaktTlf;
    List<Arbeidsforhold> arbeidsforhold = new ArrayList<>();
}
