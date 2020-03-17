package no.nav.permitteringsskjemaapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermitteringsskjemaTest {
    @Test
    void skal_ikke_kunne_endres_når_allerede_sendt_inn() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.setSendtInn(true);
        assertThatThrownBy(() -> skjema.endre(EndreSkjema.builder().build())).isInstanceOf(RuntimeException.class);
    }

    @Test
    void skal_kunne_sendes_inn_når_alt_er_fylt_ut() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.sendInn();
        assertThat(skjema.isSendtInn()).isTrue();
    }

    @Test
    void skal_ikke_kunne_sendes_inn_når_det_mangler_noe() {
        Permitteringsskjema skjema = TestData.enPermitteringMedAltFyltUt();
        skjema.setKontaktNavn("");
        assertThatThrownBy(skjema::sendInn).isInstanceOf(RuntimeException.class);
    }
}