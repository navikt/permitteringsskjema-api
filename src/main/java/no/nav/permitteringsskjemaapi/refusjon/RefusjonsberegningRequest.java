package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Value;

import java.time.LocalDate;

@Value
public class RefusjonsberegningRequest {
    String fnr;
    String bedriftNr;
    Integer gradering;
    LocalDate permitteringsperiodeStart;
    LocalDate permitteringsperiodeSlutt;
}
