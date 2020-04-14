package no.nav.permitteringsskjemaapi.refusjon;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
@Slf4j
public class FakeRefusjonsberegningClient implements RefusjonsberegningClient {
    private static final BigDecimal EN_G_DAGSBELØP = new BigDecimal(384);
    private static final BigDecimal SEKS_G_DAGSBELØP = EN_G_DAGSBELØP.multiply(BigDecimal.valueOf(6));

    private static long tilfeldigVentetid() {
        // Mellom 1 og 2 sekund
        return (long) (1000 * new Random().nextDouble() + 1);
    }

    private static boolean størreEnn6G(BigDecimal tilfeldigInntekt) {
        return tilfeldigInntekt.compareTo(SEKS_G_DAGSBELØP) == 1;
    }

    private static BigDecimal tilfeldigDagsats() {
        // Tilfeldig mellom 1G og 7G
        return EN_G_DAGSBELØP.multiply(BigDecimal.valueOf(new Random().nextDouble() * 7 + 1));
    }

    private static boolean tilfeldigFeil() {
        // 10% sjanse for "feil"
        return new Random().nextInt(9) == 1;
    }


    @Override
    @SneakyThrows
    public RefusjonsberegningResponse beregnRefusjon(RefusjonsberegningRequest request) {
        Thread.sleep(tilfeldigVentetid());

        Set<Beregningsdetalj> beregningsdetaljer = new HashSet<>();
        BigDecimal dagsinntekt = tilfeldigDagsats();

        BigDecimal avkortetDagsinntekt;
        if (størreEnn6G(dagsinntekt)) {
            beregningsdetaljer.add(Beregningsdetalj.SEKS_G);
            avkortetDagsinntekt = SEKS_G_DAGSBELØP;
        } else if (tilfeldigFeil()) {
            beregningsdetaljer.add(Beregningsdetalj.FEILET);
            avkortetDagsinntekt = null;
        } else {
            // Ingen avkorting
            avkortetDagsinntekt = dagsinntekt;
        }

        LocalDate refusjonsperiodeStart = request.getPermitteringsperiodeStart().plusDays(2);
        LocalDate maksdatoRefusjon = request.getPermitteringsperiodeStart().plusDays(20);

        LocalDate refusjonsperiodeSlutt = request.getPermitteringsperiodeSlutt().isAfter(maksdatoRefusjon)
                ? maksdatoRefusjon
                : request.getPermitteringsperiodeSlutt();

        Period refusjonsperiode = Period.between(refusjonsperiodeStart, refusjonsperiodeSlutt);
        BigDecimal refusjonsbeløpIPeriode = avkortetDagsinntekt != null ? avkortetDagsinntekt
                .multiply(new BigDecimal(request.getGradering()).divide(BigDecimal.valueOf(100))) // dagsats gradert
                .multiply(BigDecimal.valueOf(refusjonsperiode.getDays())) // ganget opp med antall dager
                .setScale(2, RoundingMode.CEILING) : null;

        BigDecimal inntektIPeriode = avkortetDagsinntekt != null ? dagsinntekt.multiply(BigDecimal.valueOf(refusjonsperiode.getDays())) // ganget opp med antall dager
                .setScale(2, RoundingMode.CEILING) : null;

        return RefusjonsberegningResponse.builder()
                .fnr(request.getFnr())
                .bedriftNr(request.getBedriftNr())
                .tidspunkt(Instant.now())
                .innhentetInntekt(inntektIPeriode)
                .beregningsdetaljer(beregningsdetaljer)
                .refusjonsbeløp(refusjonsbeløpIPeriode)
                .refusjonsperiodeStart(refusjonsperiodeStart)
                .refusjonsperiodeSlutt(refusjonsperiodeSlutt)
                .build();
    }
}
