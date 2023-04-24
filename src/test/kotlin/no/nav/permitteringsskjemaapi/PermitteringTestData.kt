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
        return Permitteringsskjema(
            id = UUID.randomUUID(),
            opprettetTidspunkt = Instant.now(),
            bedriftNr = "999999999",
            type = PermitteringsskjemaType.PERMITTERING_UTEN_LØNN,
            kontaktNavn = "Tore Toresen",
            kontaktTlf = "66778899",
            kontaktEpost = "per@bedrift.no",
            varsletAnsattDato = LocalDate.of(2020, 3, 16),
            varsletNavDato = LocalDate.of(2020, 9, 21),
            startDato = LocalDate.of(2020, 3, 17),
            sluttDato = LocalDate.of(2020, 9, 18),
            ukjentSluttDato = false,
            fritekst = "Fritekst",
            antallBerørt = 1,
            årsakskode = Årsakskode.MANGEL_PÅ_ARBEID,
        ).apply {
            val enYrkeskategori = enYrkeskategori()
            enYrkeskategori.permitteringsskjema = this
            yrkeskategorier = mutableListOf(enYrkeskategori)
        }
    }

    fun enPermitteringMedIkkeAltFyltUt() = enPermitteringMedAltFyltUt().apply {
        type = null
    }

    private fun enYrkeskategori() = Yrkeskategori(
        id = UUID.randomUUID(),
        konseptId = 1000,
        styrk08 = "0001",
        label = "Label",
    )
}