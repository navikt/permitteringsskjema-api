package no.nav.permitteringsskjemaapi.refusjon;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class FakeInntektskomponentClient implements InntektskomponentClient {
    @Override
    @SneakyThrows
    public BigDecimal hentInntekt() {
        Thread.sleep((long) (1000 * new Random().nextGaussian()));
        return new BigDecimal(10000 * new Random().nextGaussian());
    }
}
