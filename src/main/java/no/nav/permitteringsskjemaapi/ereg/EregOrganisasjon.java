package no.nav.permitteringsskjemaapi.ereg;

import lombok.Data;
import lombok.Value;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class EregOrganisasjon {
    String organisasjonsnummer;
    EnhetsRegisterNavn navn;

    public String hentNavn() {
        return Stream.of(navn.getNavnelinje1(), navn.getNavnelinje2(), navn.getNavnelinje3(), navn.getNavnelinje4())
                .filter(navnelinje -> navnelinje != null)
                .collect(Collectors.joining(" "));
    }
}
