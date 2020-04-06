package no.nav.permitteringsskjemaapi.refusjon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.ArbeidsforholdBeregnet;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.ArbeidsforholdOpprettet;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Entity
public class Arbeidsforhold extends AbstractAggregateRoot {
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Beregningsdetalj> beregningsdetaljer = EnumSet.noneOf(Beregningsdetalj.class);

    public static List<Arbeidsforhold> opprett(Refusjonsskjema skjema, List<String> fødselsnumre, Integer gradering, LocalDate periodeStart, LocalDate periodeSlutt) {
        return fødselsnumre.stream().map(fnr -> {
            var refusjonberegning = new Arbeidsforhold();
            refusjonberegning.setId(UUID.randomUUID());
            refusjonberegning.setOpprettetAv(skjema.getOpprettetAv());
            refusjonberegning.setRefusjonsskjemaId(skjema.getId());
            refusjonberegning.setBedriftNr(skjema.getBedriftNr());
            refusjonberegning.setFnr(fnr);
            refusjonberegning.setGradering(gradering);
            refusjonberegning.setPeriodeStart(periodeStart);
            refusjonberegning.setPeriodeSlutt(periodeSlutt);
            refusjonberegning.registerEvent(new ArbeidsforholdOpprettet(refusjonberegning));
            return refusjonberegning;
        }).collect(Collectors.toList());
    }

    public void endreInnhentetBeløp(BigDecimal innhentetBeløp) {
        setInnhentetTidspunkt(Instant.now());
        setInntektInnhentet(innhentetBeløp);
        beregnRefusjon();
        registerEvent(new ArbeidsforholdBeregnet(this));
    }

    private void beregnRefusjon() {
        // Fyll ut med skikkelig logikk her...
        BigDecimal graderingIDesimal = BigDecimal.valueOf(gradering).divide(BigDecimal.valueOf(100));
        BigDecimal beregnet = inntektInnhentet.multiply(graderingIDesimal);

        // Er over 6G på liksom
        if (beregnet.intValue() > 8000) {
            beregningsdetaljer.add(Beregningsdetalj.SEKS_G);
            beregnet = new BigDecimal(8000);
        }

        setRefusjonsbeløp(beregnet.setScale(2, RoundingMode.CEILING));
    }
}
