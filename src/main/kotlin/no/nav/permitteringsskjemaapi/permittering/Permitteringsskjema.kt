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
    val trukketTidspunkt: Instant? = null,

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

data class PermitteringJournalpost(
    val tittel: String,
    val dokument: PermitteringJournalpostDokument
)

data class PermitteringJournalpostDokument(
    val tittel: String,
    val brevkode: String,
)

enum class SkjemaType(
    val merkelapp: String,
    val tittel: String,
    val beskjedTekst: String,
    val trukketTekst: String,
    val innsendtPermitteringJournalpost: PermitteringJournalpost,
    val trukketPermitteringJournalpost: PermitteringJournalpost,
) {
    MASSEOPPSIGELSE(
        "Nedbemanning",
        "Melding om oppsigelse",
        "Nav har mottatt deres melding om oppsigelse. Vi tar kontakt snart.",
        "Meldingen om oppsigelse er trukket tilbake. Nav har registrert dette.",
        PermitteringJournalpost(
            "Arbeidsgivers meldeplikt til NAV ved masseoppsigelser",
            PermitteringJournalpostDokument(
                "Arbeidsgivers meldeplikt til NAV ved masseoppsigelser",
                "NAV 76-08.03"
            )
        ),
        PermitteringJournalpost(
            "Arbeidsgiver har trukket melding om masseoppsigelser til Nav",
            PermitteringJournalpostDokument(
                "Arbeidsgiver har trukket melding om masseoppsigelser til Nav",
                "NAV 76-08.03"
            )
        )
    ),
    PERMITTERING_UTEN_LØNN(
        "Permittering",
        "Melding om permittering",
        "Nav har mottatt deres melding om permittering. Vi tar kontakt snart.",
        "Meldingen om permittering er trukket tilbake. Nav har registrert dette.",
        PermitteringJournalpost(
            "Arbeidsgivers meldeplikt til Nav ved permitteringer uten lønn",
            PermitteringJournalpostDokument(
                "Arbeidsgivers meldeplikt til Nav ved permitteringer uten lønn",
                "NAV 76-08.03"
            )
        ),
        PermitteringJournalpost(
            "Arbeidsgiver har trukket melding om permitteringer uten lønn til Nav",
            PermitteringJournalpostDokument(
                "Arbeidsgiver har trukket melding om permitteringer uten lønn til Nav",
                "NAV 76-08.03"
            )
        )
    ),
    INNSKRENKNING_I_ARBEIDSTID(
        "Innskrenking av arbeidstid",
        "Melding om innskrenking av arbeidstid",
        "Nav har mottatt deres melding om innskrenking av arbeidstid.",
        "Meldingen om innskrenking av arbeidstid er trukket tilbake. Nav har registrert dette.",
        PermitteringJournalpost(
            "Arbeidsgivers meldeplikt til Nav ved innskrenking i arbeidstiden",
            PermitteringJournalpostDokument(
                "Arbeidsgivers meldeplikt til Nav ved innskrenking i arbeidstiden",
                "NAV 76-08.03"
            )
        ),
        PermitteringJournalpost(
            "Arbeidsgiver har trukket melding om innskrenking i arbeidstiden til Nav",
            PermitteringJournalpostDokument(
                "Arbeidsgiver har trukket melding om innskrenking i arbeidstiden til Nav",
                "NAV 76-08.03"
            )
        )
    )
}

enum class HendelseType {
    INNSENDT,
    TRUKKET
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
)