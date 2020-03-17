package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.AbstractAggregateRoot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermittertPerson extends AbstractAggregateRoot<PermittertPerson> {
    private Instant opprettetTidspunkt;
    private String orgNr;
    private SkjemaType type;
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
