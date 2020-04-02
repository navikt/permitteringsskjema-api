package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Data;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
public class Refusjonsberegning extends AbstractAggregateRoot {
    @Id
    private UUID id;

    private UUID refusjonsskjemaId;
    private String bedriftNr;
    private String fnr;
    private Integer gradering;
    private LocalDate periodeStart;
    private LocalDate periodeSlutt;

    private Instant innhentetTidspunkt;
    private BigDecimal inntektInnhentet;
    private BigDecimal inntektKorrigert;
    private BigDecimal refusjonsbeløp;

    public static Refusjonsberegning opprett(Arbeidsforhold arbeidsforhold) {
        var refusjonberegning = new Refusjonsberegning();
        refusjonberegning.setId(UUID.randomUUID());
        refusjonberegning.setRefusjonsskjemaId(arbeidsforhold.getRefusjonsskjema().getId());
        refusjonberegning.setBedriftNr(arbeidsforhold.getRefusjonsskjema().getBedriftNr());
        refusjonberegning.setFnr(arbeidsforhold.getFnr());
        refusjonberegning.setGradering(arbeidsforhold.getGradering());
        refusjonberegning.setPeriodeStart(arbeidsforhold.getPeriodeStart());
        refusjonberegning.setPeriodeSlutt(arbeidsforhold.getPeriodeSlutt());
        return refusjonberegning;
    }
}
