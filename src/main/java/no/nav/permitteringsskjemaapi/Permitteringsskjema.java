package no.nav.permitteringsskjemaapi;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.springframework.data.domain.AbstractAggregateRoot;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaEndret;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaOpprettet;

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

    @OneToMany(mappedBy = "permitteringsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Person> personer = new ArrayList<>();

    public static Permitteringsskjema nyttSkjema(String orgNr) {
        Permitteringsskjema skjema = new Permitteringsskjema();
        skjema.setId(UUID.randomUUID());
        skjema.setOpprettetTidspunkt(Instant.now());
        skjema.setOrgNr(orgNr);
        skjema.registerEvent(new SkjemaOpprettet(skjema));
        return skjema;
    }

    public void endre(EndreSkjema endreSkjema) {
        setType(endreSkjema.getType());
        setKontaktNavn(endreSkjema.getKontaktNavn());
        setKontaktTlf(endreSkjema.getKontaktTlf());
        setVarsletAnsattDato(endreSkjema.getVarsletAnsattDato());
        setVarsletNavDato(endreSkjema.getVarsletNavDato());
        setStartDato(endreSkjema.getStartDato());
        setSluttDato(endreSkjema.getSluttDato());
        setUkjentSluttDato(endreSkjema.getUkjentSluttDato());
        setFritekst(endreSkjema.getFritekst());
        personer.clear();
        personer.addAll(endreSkjema.getPersoner());
        personer.forEach(p -> p.setId(UUID.randomUUID()));
        personer.forEach(p -> p.setPermitteringsskjema(this));
        registerEvent(new SkjemaEndret(this));
    }

    public List<PermittertPerson> permittertePersoner() {
        return personer.stream()
                .map(this::tilPermittertPerson)
                .collect(Collectors.toList());
    }

    private PermittertPerson tilPermittertPerson(Person p) {
        return PermittertPerson.builder()
                .person(p)
                .fritekst(fritekst)
                .kontaktNavn(kontaktNavn)
                .kontaktTlf(kontaktTlf)
                .opprettetTidspunkt(opprettetTidspunkt)
                .orgNr(orgNr)
                .sluttDato(sluttDato)
                .startDato(startDato)
                .type(type)
                .ukjentSluttDato(ukjentSluttDato)
                .varsletAnsattDato(varsletAnsattDato)
                .varsletNavDato(varsletNavDato)
                .build();

    }
}
