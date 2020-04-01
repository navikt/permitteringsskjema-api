package no.nav.permitteringsskjemaapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
public class Arbeidsforhold {
    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "refusjonsskjema_id")
    @JsonIgnore
    @ToString.Exclude
    private Refusjonsskjema refusjonsskjema;

    private String fnr;
    private Integer gradering;
    private Instant inntektInnhentetTidspunkt;
    private Integer inntektFraRegister;
    private Integer inntektFraArbeidsgiver;
    private LocalDate periodeStart;
    private LocalDate periodeSlutt;
}
