package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver

import jakarta.validation.constraints.NotNull
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import org.springframework.lang.Nullable
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class ArbeidsgiverRapport(
    var id: @NotNull UUID? = null,
    var bedriftsnummer: @NotNull String? = null,
    var sendtInnTidspunkt: @NotNull Instant? = null,
    var type: @NotNull PermitteringsskjemaType? = null,
    var kontaktNavn: @NotNull String? = null,
    var kontaktTlf: @NotNull String? = null,
    var kontaktEpost: @NotNull String? = null,
    var varsletAnsattDato: @NotNull LocalDate? = null,
    var varsletNavDato: @NotNull LocalDate? = null,
    var startDato: @NotNull LocalDate? = null,
    @Nullable var sluttDato: LocalDate? = null,
    @Nullable var fritekst: String? = null,
    var antallBerorte: @NotNull Int? = null,
    var årsakskode: @NotNull Årsakskode? = null,
    @Nullable var årsakstekst: String? = null,
    var yrkeskategorier: @NotNull List<Yrkeskategori>? = null,
)