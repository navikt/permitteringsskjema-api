package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Value
@Builder
public class RefusjonsberegningResponse {
    String fnr;
    String bedriftNr;
    Instant tidspunkt;
    BigDecimal innhentetInntekt;
    Set<Beregningsdetalj> beregningsdetaljer;
    BigDecimal refusjonsbeløp;
    LocalDate refusjonsperiodeStart;
    LocalDate refusjonsperiodeSlutt;
}
