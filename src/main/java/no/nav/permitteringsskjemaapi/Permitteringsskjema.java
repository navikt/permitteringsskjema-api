package no.nav.permitteringsskjemaapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaEndret;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaOpprettet;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import no.nav.permitteringsskjemaapi.util.ObjektUtils;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Lombok
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
// JPA
@Entity
public class Permitteringsskjema extends AbstractAggregateRoot<Permitteringsskjema> {
    private String fritekst;
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String kontaktNavn;
    private String kontaktTlf;
    private String kontaktEpost;
    private Instant opprettetTidspunkt;
    private String bedriftNr;
    private String bedriftNavn;
    @OneToMany(mappedBy = "permitteringsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Person> personer = new ArrayList<>();
    private boolean sendtInn;
    private LocalDate sluttDato;
    private LocalDate startDato;
    @Enumerated(EnumType.STRING)
    private SkjemaType type;
    private boolean ukjentSluttDato;
    private LocalDate varsletAnsattDato;
    private LocalDate varsletNavDato;

    @JsonProperty
    public Integer antallBerørt() {
        return personer.size();
    }

    public static Permitteringsskjema opprettSkjema(OpprettSkjema opprettSkjema) {
        Permitteringsskjema skjema = new Permitteringsskjema();
        skjema.setId(UUID.randomUUID());
        skjema.setOpprettetTidspunkt(Instant.now());
        skjema.setBedriftNr(opprettSkjema.getBedriftNr());
        skjema.setBedriftNavn("(Hentes fra Altinn)");
        skjema.setType(opprettSkjema.getType());
        skjema.registerEvent(new SkjemaOpprettet(skjema));
        return skjema;
    }

    public void endre(EndreSkjema endreSkjema) {
        sjekkOmSkjemaErSendtInn();
        setType(endreSkjema.getType());
        setKontaktNavn(endreSkjema.getKontaktNavn());
        setKontaktTlf(endreSkjema.getKontaktTlf());
        setKontaktEpost(endreSkjema.getKontaktEpost());
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
                .orgNr(bedriftNr)
                .sluttDato(sluttDato)
                .startDato(startDato)
                .type(type)
                .ukjentSluttDato(ukjentSluttDato)
                .varsletAnsattDato(varsletAnsattDato)
                .varsletNavDato(varsletNavDato)
                .build();
    }

    private void sjekkOmSkjemaErSendtInn() {
        if (sendtInn) {
            throw new RuntimeException("Skjema er allerede sendt inn");
        }
    }

    public void sendInn() {
        sjekkOmObligatoriskInformasjonErFyltUt();
        setSendtInn(true);
        registerEvent(new SkjemaSendtInn(this));
    }

    private void sjekkOmObligatoriskInformasjonErFyltUt() {
        if (ObjektUtils.isAnyEmpty(
                type,
                kontaktNavn,
                kontaktTlf,
                varsletAnsattDato,
                varsletNavDato,
                startDato,
                sluttDato,
                ukjentSluttDato,
                fritekst)) {
            throw new RuntimeException("Alle felter er ikke fylt ut");
        }
    }
}
