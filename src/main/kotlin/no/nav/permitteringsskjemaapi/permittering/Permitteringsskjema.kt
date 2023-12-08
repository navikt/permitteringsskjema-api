package no.nav.permitteringsskjemaapi.permittering

import java.time.Instant
import java.time.LocalDate
import java.util.*

data class Permitteringsskjema(
    val id: UUID,
    val type: SkjemaType,

    val bedriftNr: String,
    val bedriftNavn: String,

    val kontaktNavn: String,
    val kontaktEpost: String,
    val kontaktTlf: String,

    val antallBerørt: Int,
    val årsakskode: Årsakskode,

    val yrkeskategorier: List<Yrkeskategori>,

    val startDato: LocalDate,
    val sluttDato: LocalDate?,
    val ukjentSluttDato: Boolean,

    val sendtInnTidspunkt: Instant,
    val opprettetAv: String,
) {
    val fritekst = """
        ### Yrker
        ${yrkeskategorier.joinToString(", ") { it.label }}
        ### Årsak
        ${årsakskode.navn}
    """.trimIndent()

    val årsakstekst = årsakskode.navn
}

enum class SkjemaType(val navn: String) {
    MASSEOPPSIGELSE("Masseoppsigelse"),
    PERMITTERING_UTEN_LØNN("Permittering uten lønn"),
    INNSKRENKNING_I_ARBEIDSTID("Innskrenkning i arbeidstid")
}

enum class Årsakskode(val navn: String) {
    MANGEL_PÅ_ARBEID("Mangel på arbeid eller oppdrag"),
    RÅSTOFFMANGEL("Råstoffmangel"),
    ARBEIDSKONFLIKT_ELLER_STREIK("Arbeidskonflikt eller streik"),
    BRANN("Brann"),
    PÅLEGG_FRA_OFFENTLIG_MYNDIGHET("Pålegg fra offentlig myndighet"),
    ANDRE_ÅRSAKER("Andre årsaker")

}

data class Yrkeskategori(
    val konseptId: Int,
    val styrk08: String,
    val label: String,
) {
    // ligger igjen fra gammel modell
    // denne er eksponert i kafka melding, selv om den alltid er null
    // TODO: vurder om denne kan fjernes helt. kafka schema test er grønn uten denne
    //val antall : Int? = null
}