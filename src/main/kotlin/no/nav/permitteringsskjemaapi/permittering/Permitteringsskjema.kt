package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException
import org.apache.commons.lang3.ObjectUtils
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
class Permitteringsskjema(
    var antallBerørt: Int? = null,
    var avbrutt: Boolean = false,
    var bedriftNavn: String? = null,
    var bedriftNr: String? = null,
    var fritekst: String? = null,
    @field:Id
    var id: UUID? = null,
    var kontaktEpost: String? = null,
    var kontaktNavn: String? = null,
    var kontaktTlf: String? = null,
    @field:JsonIgnore
    var opprettetAv: String? = null,
    var opprettetTidspunkt: Instant? = null,
    var sendtInnTidspunkt: Instant? = null,
    var sluttDato: LocalDate? = null,
    var startDato: LocalDate? = null,
    @field:Enumerated(EnumType.STRING)
    var type: PermitteringsskjemaType? = null,
    var ukjentSluttDato: Boolean = false,
    var varsletAnsattDato: LocalDate? = null,
    var varsletNavDato: LocalDate? = null,
    @field:OneToMany(
        mappedBy = "permitteringsskjema",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var yrkeskategorier: List<Yrkeskategori> = listOf(),
    @field:Enumerated(EnumType.STRING)
    var årsakskode: Årsakskode? = null,
    var årsakstekst: String? = null,
) : AbstractAggregateRoot<Permitteringsskjema?>() {


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
        yrkeskategorier = endreSkjema.yrkeskategorier.map { it.copy(id = UUID.randomUUID(), permitteringsskjema = this) }

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