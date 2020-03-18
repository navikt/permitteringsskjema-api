package no.nav.permitteringsskjemaapi;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.*;

class PermitteringsskjemaTest {
    @Test
    void skal_ikke_kunne_endres_når_allerede_sendt_inn() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.setSendtInnTidspunkt(Instant.now());
        assertThatThrownBy(() -> skjema.endre(EndreSkjema.builder().build())).isInstanceOf(RuntimeException.class);
    }

    @Test
    void skal_kunne_sendes_inn_når_alt_er_fylt_ut() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.sendInn();

        // 100 ms er valgt litt vilkårlig, bare så det ikke sammenlignes eksakt på nanosekundet
        assertThat(skjema.getSendtInnTidspunkt()).isCloseTo(Instant.now(), within(100, MILLIS));
    }

    @Test
    void skal_ikke_kunne_sendes_inn_når_det_mangler_noe() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.setKontaktNavn("");
        assertThatThrownBy(skjema::sendInn).isInstanceOf(RuntimeException.class);
    }
}