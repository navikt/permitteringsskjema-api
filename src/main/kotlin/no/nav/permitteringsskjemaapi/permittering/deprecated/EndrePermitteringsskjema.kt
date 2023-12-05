package no.nav.permitteringsskjemaapi.permittering.deprecated

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import java.time.LocalDate

data class EndrePermitteringsskjema(
    var type: PermitteringsskjemaType? = null,
    var kontaktNavn: String? = null,
    var kontaktTlf: String? = null,
    var kontaktEpost: String? = null,
    var varsletAnsattDato: LocalDate? = null,
    var varsletNavDato: LocalDate? = null,
    var startDato: LocalDate? = null,
    var sluttDato: LocalDate? = null,
    var ukjentSluttDato: Boolean? = null,
    var fritekst: String? = null,
    var antallBerørt: Int? = null,
    var yrkeskategorier: List<Yrkeskategori> = listOf(),
    var årsakskode: Årsakskode? = null,
    var årsakstekst: String? = null,
)