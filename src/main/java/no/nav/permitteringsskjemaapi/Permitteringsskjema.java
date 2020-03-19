package no.nav.permitteringsskjemaapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaAvbrutt;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaEndret;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaOpprettet;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException;
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException;
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
    private boolean avbrutt;
    private String bedriftNavn;
    private String bedriftNr;
    private String fritekst;
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String kontaktEpost;
    private String kontaktNavn;
    private String kontaktTlf;
    @JsonIgnore
    private String opprettetAv;
    private Instant opprettetTidspunkt;
    @OneToMany(mappedBy = "permitteringsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Person> personer = new ArrayList<>();
    private Instant sendtInnTidspunkt;
    private LocalDate sluttDato;
    private LocalDate startDato;
    @Enumerated(EnumType.STRING)
    private SkjemaType type;
    private boolean ukjentSluttDato;
    private LocalDate varsletAnsattDato;
    private LocalDate varsletNavDato;

    public static Permitteringsskjema opprettSkjema(OpprettSkjema opprettSkjema, String utførtAv) {
        Permitteringsskjema skjema = new Permitteringsskjema();
        skjema.setId(UUID.randomUUID());
        skjema.setOpprettetTidspunkt(Instant.now());
        skjema.setOpprettetAv(utførtAv);
        skjema.setBedriftNr(opprettSkjema.getBedriftNr());
        skjema.setType(opprettSkjema.getType());
        skjema.registerEvent(new SkjemaOpprettet(skjema, utførtAv));
        return skjema;
    }

    @JsonProperty
    public Integer antallBerørt() {
        return personer.size();
    }

    public void endre(EndreSkjema endreSkjema, String utførtAv) {
        sjekkOmSkjemaErAvbrutt();
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
        registerEvent(new SkjemaEndret(this, utførtAv));
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
        if (sendtInnTidspunkt != null) {
            throw new RuntimeException("Skjema er allerede sendt inn");
        }
    }

    public void sendInn(String utførtAv) {
        sjekkOmSkjemaErAvbrutt();
        sjekkOmObligatoriskInformasjonErFyltUt();
        setSendtInnTidspunkt(Instant.now());
        registerEvent(new SkjemaSendtInn(this, utførtAv));
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
            throw new AlleFelterIkkeFyltUtException();
        }
    }

    private void sjekkOmSkjemaErAvbrutt() {
        if (avbrutt) {
            throw new SkjemaErAvbruttException();
        }
    }

    public void avbryt(String utførtAv) {
        sjekkOmSkjemaErAvbrutt();
        setAvbrutt(true);
        registerEvent(new SkjemaAvbrutt(this, utførtAv));
    }
}
