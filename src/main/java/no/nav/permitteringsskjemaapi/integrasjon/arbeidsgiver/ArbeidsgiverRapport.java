package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import lombok.Builder;
import lombok.Value;
import no.nav.permitteringsskjemaapi.SkjemaType;
import no.nav.permitteringsskjemaapi.Årsakskode;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class ArbeidsgiverRapport {

    @NotNull
    UUID id;
    @NotNull
    String bedriftsnummer;
    @NotNull
    Instant sendtInnTidspunkt;
    @NotNull
    SkjemaType type;
    @NotNull
    String kontaktNavn;
    @NotNull
    String kontaktTlf;
    @NotNull
    String kontaktEpost;
    @NotNull
    LocalDate varsletAnsattDato;
    @NotNull
    LocalDate varsletNavDato;
    @NotNull
    LocalDate startDato;
    @Nullable
    LocalDate sluttDato;
    @Nullable
    String fritekst;
    @NotNull
    Integer antallBerorte;
    @Nullable
    Årsakskode årsakskode;
    @Nullable
    String årsakstekst;
}
