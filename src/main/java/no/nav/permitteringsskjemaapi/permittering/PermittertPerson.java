package no.nav.permitteringsskjemaapi.permittering;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class PermittertPerson {
    private Instant opprettetTidspunkt;
    private String orgNr;
    private PermitteringsskjemaType type;
    private String kontaktNavn;
    private String kontaktTlf;
    private LocalDate varsletAnsattDato;
    private LocalDate varsletNavDato;
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Boolean ukjentSluttDato;
    private String fritekst;
    private Person person;

}
