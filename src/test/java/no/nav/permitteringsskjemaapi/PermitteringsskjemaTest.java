package no.nav.permitteringsskjemaapi;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException;
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException;

class PermitteringsskjemaTest {
    @Test
    void skal_ikke_kunne_endres_når_allerede_sendt_inn() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.setSendtInnTidspunkt(Instant.now());
        assertThatThrownBy(() -> skjema.endre(EndreSkjema.builder().build(), "")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void skal_kunne_sendes_inn_når_alt_er_fylt_ut() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.sendInn("");

        // 100 ms er valgt litt vilkårlig, bare så det ikke sammenlignes eksakt på
        // nanosekundet
        assertThat(skjema.getSendtInnTidspunkt()).isCloseTo(Instant.now(), within(100, MILLIS));
    }

    @Test
    void skal_ikke_kunne_sendes_inn_når_det_mangler_noe() {
        Permitteringsskjema skjema = TestData.enPermitteringMedIkkeAltFyltUt();
        skjema.setKontaktNavn("");
        assertThatThrownBy(() -> skjema.sendInn("")).isInstanceOf(AlleFelterIkkeFyltUtException.class);
    }

    @Test
    void skal_kunne_avbrytes() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.avbryt("");
        assertThat(skjema.isAvbrutt());
    }

    @Test
    void skal_ikke_kunne_endres_etter_at_det_er_avbrutt() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.avbryt("");
        assertThatThrownBy(() -> skjema.endre(EndreSkjema.builder().build(), ""))
                .isInstanceOf(SkjemaErAvbruttException.class);
    }
}