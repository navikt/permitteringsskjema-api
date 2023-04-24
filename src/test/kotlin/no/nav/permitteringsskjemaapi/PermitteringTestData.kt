package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Yrkeskategori
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import java.time.Instant
import java.time.LocalDate
import java.util.*

object PermitteringTestData {
    fun enPermitteringMedAltFyltUt(): Permitteringsskjema {
        val permitteringsskjema = Permitteringsskjema()
        permitteringsskjema.id = UUID.randomUUID()
        permitteringsskjema.opprettetTidspunkt = Instant.now()
        permitteringsskjema.bedriftNr = "999999999"
        permitteringsskjema.type = PermitteringsskjemaType.PERMITTERING_UTEN_LØNN
        permitteringsskjema.kontaktNavn = "Tore Toresen"
        permitteringsskjema.kontaktTlf = "66778899"
        permitteringsskjema.kontaktEpost = "per@bedrift.no"
        permitteringsskjema.varsletAnsattDato = LocalDate.of(2020, 3, 16)
        permitteringsskjema.varsletNavDato = LocalDate.of(2020, 9, 21)
        permitteringsskjema.startDato = LocalDate.of(2020, 3, 17)
        permitteringsskjema.sluttDato = LocalDate.of(2020, 9, 18)
        permitteringsskjema.ukjentSluttDato = false
        permitteringsskjema.fritekst = "Fritekst"
        permitteringsskjema.antallBerørt = 1
        permitteringsskjema.årsakskode = Årsakskode.MANGEL_PÅ_ARBEID
        val enYrkeskategori = enYrkeskategori()
        enYrkeskategori.permitteringsskjema = permitteringsskjema
        permitteringsskjema.yrkeskategorier = mutableListOf(enYrkeskategori)
        return permitteringsskjema
    }

    fun enPermitteringMedIkkeAltFyltUt(): Permitteringsskjema {
        val skjema = enPermitteringMedAltFyltUt()
        skjema.type = null
        return skjema
    }

    private fun enYrkeskategori(): Yrkeskategori {
        val yrkeskategori = Yrkeskategori()
        yrkeskategori.id = UUID.randomUUID()
        yrkeskategori.konseptId = 1000
        yrkeskategori.styrk08 = "0001"
        yrkeskategori.label = "Label"
        return yrkeskategori
    }
}