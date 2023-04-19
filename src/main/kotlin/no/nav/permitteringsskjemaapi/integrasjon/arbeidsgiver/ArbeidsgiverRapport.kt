package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType;
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori;
import no.nav.permitteringsskjemaapi.permittering.Årsakskode;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    PermitteringsskjemaType type;
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
    @NotNull
    Årsakskode årsakskode;
    @Nullable
    String årsakstekst;
    @NotNull
    List<Yrkeskategori> yrkeskategorier;
}
