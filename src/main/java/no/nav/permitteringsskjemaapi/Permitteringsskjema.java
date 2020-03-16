package no.nav.permitteringsskjemaapi;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Lombok
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
// JPA
@Entity
public class Permitteringsskjema extends AbstractAggregateRoot<Permitteringsskjema> {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private Instant opprettetTidspunkt;
    private String orgNr;
    @Enumerated(EnumType.STRING)
    private SkjemaType type;
    private String kontaktNavn;
    private String kontaktTlf;
    private LocalDate varsletAnsattDato;
    private LocalDate varsletNavDato;
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Boolean ukjentSluttDato;
    private String fritekst;
}
