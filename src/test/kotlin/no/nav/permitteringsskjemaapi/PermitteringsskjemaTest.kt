package no.nav.permitteringsskjemaapi

import no.nav.permitteringsskjemaapi.deprecated.EndrePermitteringsskjema
import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class PermitteringsskjemaTest {
    @Test
    fun skal_ikke_kunne_endres_når_allerede_sendt_inn() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.sendtInnTidspunkt = Instant.now()
        Assertions.assertThatThrownBy {
            skjema.endre(EndrePermitteringsskjema())
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun skal_ikke_kunne_sendes_inn_når_allerede_sendt_inn() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.sendtInnTidspunkt = Instant.now()
        Assertions.assertThatThrownBy { skjema.sendInn() }.isInstanceOf(
            RuntimeException::class.java
        )
    }

    @Test
    fun skal_kunne_sendes_inn_når_alt_er_fylt_ut() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.sendInn()

        // 100 ms er valgt litt vilkårlig, bare så det ikke sammenlignes eksakt på
        // nanosekundet
        Assertions.assertThat(skjema.sendtInnTidspunkt).isCloseTo(Instant.now(), Assertions.within(100, ChronoUnit.MILLIS))
    }

    @Test
    fun skal_ikke_kunne_sendes_inn_når_det_mangler_noe() {
        val skjema = PermitteringTestData.enPermitteringMedIkkeAltFyltUt()
        skjema.kontaktNavn = ""
        Assertions.assertThatThrownBy {
            skjema.sendInn()
        }.isInstanceOf(AlleFelterIkkeFyltUtException::class.java)
    }

    @Test
    fun skal_kunne_avbrytes() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.avbryt()
        Assertions.assertThat(skjema.avbrutt)
    }

    @Test
    fun skal_ikke_kunne_endres_etter_at_det_er_avbrutt() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.avbrutt = true
        Assertions.assertThatThrownBy {
            skjema.endre(EndrePermitteringsskjema())
        }.isInstanceOf(SkjemaErAvbruttException::class.java)
    }

    @Test
    fun skal_ikke_kunne_avbrytes_etter_at_det_er_sendt_inn() {
        val skjema = PermitteringTestData.enPermitteringMedAltFyltUt()
        skjema.sendtInnTidspunkt = Instant.now()
        Assertions.assertThatThrownBy {
            skjema.avbryt()
        }.isInstanceOf(RuntimeException::class.java)
    }
}