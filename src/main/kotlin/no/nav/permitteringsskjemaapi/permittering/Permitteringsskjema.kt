package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.deprecated.EndrePermitteringsskjema
import no.nav.permitteringsskjemaapi.deprecated.OpprettPermitteringsskjema
import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException
import org.apache.commons.lang3.ObjectUtils
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

    fun endre(endreSkjema: EndrePermitteringsskjema) {
        sjekkOmSkjemaErAvbrutt()
        sjekkOmSkjemaErSendtInn()
        type = endreSkjema.type
        kontaktNavn = endreSkjema.kontaktNavn
        kontaktTlf = endreSkjema.kontaktTlf
        kontaktEpost = endreSkjema.kontaktEpost
        varsletAnsattDato = endreSkjema.varsletAnsattDato
        varsletNavDato = endreSkjema.varsletNavDato
        startDato = endreSkjema.startDato
        sluttDato = endreSkjema.sluttDato
        ukjentSluttDato = endreSkjema.sluttDato == null
        fritekst = endreSkjema.fritekst
        antallBerørt = endreSkjema.antallBerørt
        årsakskode = endreSkjema.årsakskode
        årsakstekst = endreSkjema.årsakstekst
        yrkeskategorier.clear()
        yrkeskategorier.addAll(
            endreSkjema.yrkeskategorier.map { it.copy(id = UUID.randomUUID(), permitteringsskjema = this) }
        )

    }

    private fun sjekkOmSkjemaErSendtInn() {
        if (sendtInnTidspunkt != null) {
            throw RuntimeException("Skjema er allerede sendt inn")
        }
    }

    fun sendInn() {
        sjekkOmSkjemaErSendtInn()
        sjekkOmSkjemaErAvbrutt()
        sjekkOmObligatoriskInformasjonErFyltUt()
        sendtInnTidspunkt = Instant.now()
    }

    private fun sjekkOmObligatoriskInformasjonErFyltUt() {
        val feil: MutableList<String> = ArrayList()
        validateNotNull("Skjematype", type, feil)
        validateNotNull("Navn på kontaktperson", kontaktNavn, feil)
        validateNotNull("Telefonnummer til kontaktperson", kontaktTlf, feil)
        validateNotNull("E-post til kontaktperson", kontaktEpost, feil)
        validateNotNull("Startdato", startDato, feil)
        if (!ukjentSluttDato) {
            validateNotNull("Sluttdato", sluttDato, feil)
        }
        validateNotNull("Hvorfor det skal permitteres og hvilke yrkeskategorier som er berørt", fritekst, feil)
        validateNotNull("Antall berørt", antallBerørt, feil)
        validateNotNull("Årsakskode", årsakskode, feil)
        if (yrkeskategorier.isEmpty()) {
            feil.add("Yrkeskategori")
        }
        if (feil.isEmpty()) {
            return
        }
        throw AlleFelterIkkeFyltUtException(feil)
    }

    private fun validateNotNull(desc: String, `object`: Any?, feil: MutableList<String>) {
        if (ObjectUtils.isEmpty(`object`)) {
            log.warn("Validering feilet, er null {}", desc)
            feil.add(desc)
        }
    }

    private fun sjekkOmSkjemaErAvbrutt() {
        if (avbrutt) {
            throw SkjemaErAvbruttException()
        }
    }

    fun avbryt() {
        sjekkOmSkjemaErAvbrutt()
        sjekkOmSkjemaErSendtInn()
        avbrutt = true
    }

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


    companion object {
        private val log = logger()

        fun opprettSkjema(opprettSkjema: OpprettPermitteringsskjema, utførtAv: String): Permitteringsskjema {
            val skjema = Permitteringsskjema()
            skjema.id = UUID.randomUUID()
            skjema.opprettetTidspunkt = Instant.now()
            skjema.opprettetAv = utførtAv
            skjema.bedriftNr = opprettSkjema.bedriftNr
            skjema.type = opprettSkjema.type
            return skjema
        }
    }
}