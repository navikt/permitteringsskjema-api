package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
public class LeggTilArbeidsforhold {
    UUID refusjonsskjemaId;
    List<String> fnr;
    Integer gradering;
    LocalDate periodeStart;
    LocalDate periodeSlutt;
}
