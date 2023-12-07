package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
class Permitteringsskjema(
    @field:Id
    var id: UUID? = null,

    @field:Enumerated(EnumType.STRING)
    var type: PermitteringsskjemaType? = null,

    var bedriftNr: String? = null,
    var bedriftNavn: String? = null,

    var kontaktNavn: String? = null,
    var kontaktEpost: String? = null,
    var kontaktTlf: String? = null,

    var antallBerørt: Int? = null,

    @field:Enumerated(EnumType.STRING)
    var årsakskode: Årsakskode? = null,
    var årsakstekst: String? = null,

    @field:OneToMany(
        mappedBy = "permitteringsskjema",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var yrkeskategorier: MutableList<Yrkeskategori> = mutableListOf(),

    var startDato: LocalDate? = null,
    var sluttDato: LocalDate? = null,
    var ukjentSluttDato: Boolean = false,

    var fritekst: String? = null, // ikke fritekst, kombineres maskinelt i frontend: årsakskode og yrkeskategorier
    var varsletAnsattDato: LocalDate? = null, // misvisende og bør fjernes
    var varsletNavDato: LocalDate? = null, // misvisende og bør fjernes

    @field:JsonIgnore
    var opprettetAv: String? = null,
    var opprettetTidspunkt: Instant? = null, // opprettet og sendt inn er det samme tidspunktet
    var sendtInnTidspunkt: Instant? = null, // bør slås sammen


    var avbrutt: Boolean = false, // gammel funksjonalitet. bør fjernes
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permitteringsskjema

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}