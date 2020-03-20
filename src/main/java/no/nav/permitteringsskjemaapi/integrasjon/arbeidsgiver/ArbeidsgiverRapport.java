package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import lombok.Builder;
import lombok.Value;
import no.nav.permitteringsskjemaapi.SkjemaType;

@Value
@Builder
public class ArbeidsgiverRapport {

    @NotNull
    private UUID id;
    @NotNull
    private String bedriftsnummer;
    @NotNull
    private Instant sendtInnTidspunkt;
    @NotNull
    private SkjemaType type;
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
