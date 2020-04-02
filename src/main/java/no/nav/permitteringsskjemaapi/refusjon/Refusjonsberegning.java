package no.nav.permitteringsskjemaapi.refusjon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.InntektInnhentet;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsberegningOpprettet;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
public class Refusjonsberegning extends AbstractAggregateRoot {
    @Id
    private UUID id;

    @JsonIgnore
    private String opprettetAv;
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
        refusjonberegning.setOpprettetAv(arbeidsforhold.getRefusjonsskjema().getOpprettetAv());
        refusjonberegning.setRefusjonsskjemaId(arbeidsforhold.getRefusjonsskjema().getId());
        refusjonberegning.setBedriftNr(arbeidsforhold.getRefusjonsskjema().getBedriftNr());
        refusjonberegning.setFnr(arbeidsforhold.getFnr());
        refusjonberegning.setGradering(arbeidsforhold.getGradering());
        refusjonberegning.setPeriodeStart(arbeidsforhold.getPeriodeStart());
        refusjonberegning.setPeriodeSlutt(arbeidsforhold.getPeriodeSlutt());
        refusjonberegning.registerEvent(new RefusjonsberegningOpprettet(refusjonberegning));
        return refusjonberegning;
    }

    public void endreInnhentetBeløp(BigDecimal innhentetBeløp) {
        setInnhentetTidspunkt(Instant.now());
        setInntektInnhentet(innhentetBeløp);
        beregnRefusjon();
        registerEvent(new InntektInnhentet(this));
    }

    private void beregnRefusjon() {
        // Fyll ut med skikkelig logikk her...
        setRefusjonsbeløp(inntektInnhentet.divide(BigDecimal.valueOf(2)).setScale(2, RoundingMode.CEILING));
    }
}
