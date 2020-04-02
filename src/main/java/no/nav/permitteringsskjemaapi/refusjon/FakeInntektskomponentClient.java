package no.nav.permitteringsskjemaapi.refusjon;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
@Slf4j
public class FakeInntektskomponentClient implements InntektskomponentClient {
    private static double tilfeldigTallMellom1Og2() {
        return new Random().nextDouble() + 1;
    }

    @Override
    @SneakyThrows
    public BigDecimal hentInntekt() {
        long tilfeldigVentetid = (long) (1000 * tilfeldigTallMellom1Og2());
        log.info("Kallet tar " + tilfeldigVentetid);
        Thread.sleep(tilfeldigVentetid);
        return new BigDecimal(10000 * tilfeldigTallMellom1Og2()).setScale(2, RoundingMode.CEILING);
    }
}
