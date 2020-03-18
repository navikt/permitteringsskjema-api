package no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidsgiver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ArbeidsgiverRapport {

    @NotNull
    private UUID id;
    @NotNull
    private String bedriftsnummer;
    @NotNull
    private LocalDateTime sendtInnTidspunkt;
    @NotNull
    private ArbeidsgiverSkjemaType type;
    @NotNull
    private String kontaktNavn;
    @NotNull
    private String kontaktTlf;
    @NotNull
    private String kontaktEpost;
    @NotNull
    private LocalDate varsletAnsattDato;
    @NotNull
    private LocalDate varsletNavDato;
    @NotNull
    private LocalDate startDato;
    @Nullable
    private LocalDate sluttDato;
    @Nullable
    private String fritekst;
    @NotNull
    Integer antallBerorte;
}
