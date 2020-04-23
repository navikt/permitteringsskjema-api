package no.nav.permitteringsskjemaapi.permittering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.permitteringsskjemaapi.exceptions.AlleFelterIkkeFyltUtException;
import no.nav.permitteringsskjemaapi.exceptions.SkjemaErAvbruttException;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaAvbrutt;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaEndret;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaOpprettet;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaSendtInn;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(Permitteringsskjema.class);
    private Integer antallBerørt;
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
    @OneToMany(mappedBy = "permitteringsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Person> personer = new ArrayList<>();
    private Instant sendtInnTidspunkt;
    private LocalDate sluttDato;
    private LocalDate startDato;
    @Enumerated(EnumType.STRING)
    private PermitteringsskjemaType type;
    private boolean ukjentSluttDato;
    private LocalDate varsletAnsattDato;
    private LocalDate varsletNavDato;
    @OneToMany(mappedBy = "permitteringsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Yrkeskategori> yrkeskategorier = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    private Årsakskode årsakskode;
    private String årsakstekst;

    public static Permitteringsskjema opprettSkjema(OpprettPermitteringsskjema opprettSkjema, String utførtAv) {
        Permitteringsskjema skjema = new Permitteringsskjema();
        skjema.setId(UUID.randomUUID());
        skjema.setOpprettetTidspunkt(Instant.now());
        skjema.setOpprettetAv(utførtAv);
        skjema.setBedriftNr(opprettSkjema.getBedriftNr());
        skjema.setType(opprettSkjema.getType());
        skjema.registerEvent(new PermitteringsskjemaOpprettet(skjema, utførtAv));
        return skjema;
    }

    public void endre(EndrePermitteringsskjema endreSkjema, String utførtAv) {
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
        setUkjentSluttDato(endreSkjema.getSluttDato() == null);
        setFritekst(endreSkjema.getFritekst());
        setAntallBerørt(endreSkjema.getAntallBerørt());
        setÅrsakskode(endreSkjema.getÅrsakskode());
        setÅrsakstekst(endreSkjema.getÅrsakstekst());
        yrkeskategorier.clear();
        yrkeskategorier.addAll(endreSkjema.getYrkeskategorier());
        yrkeskategorier.forEach(y -> y.setId(UUID.randomUUID()));
        yrkeskategorier.forEach(y -> y.setPermitteringsskjema(this));
        personer.clear();
        personer.addAll(endreSkjema.getPersoner());
        personer.forEach(p -> p.setId(UUID.randomUUID()));
        personer.forEach(p -> p.setPermitteringsskjema(this));
        registerEvent(new PermitteringsskjemaEndret(this, utførtAv));
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
        sjekkOmSkjemaErSendtInn();
        sjekkOmSkjemaErAvbrutt();
        sjekkOmObligatoriskInformasjonErFyltUt();
        setSendtInnTidspunkt(Instant.now());
        registerEvent(new PermitteringsskjemaSendtInn(this, utførtAv));
    }

    private void sjekkOmObligatoriskInformasjonErFyltUt() {
        List<String> feil = new ArrayList<>();
        validateNotNull("Skjematype", type, feil);
        validateNotNull("Navn på kontaktperson", kontaktNavn, feil);
        validateNotNull("Telefonnummer til kontaktperson", kontaktTlf, feil);
        validateNotNull("E-post til kontaktperson", kontaktEpost, feil);
        validateNotNull("Startdato", startDato, feil);
        if (!ukjentSluttDato) {
            validateNotNull("Sluttdato", sluttDato, feil);
        }
        validateNotNull("Hvorfor det skal permitteres og hvilke yrkeskategorier som er berørt", fritekst, feil);
        validateNotNull("Antall berørt", antallBerørt, feil);
        validateNotNull("Årsakskode", årsakskode, feil);
        if (årsakskode == Årsakskode.ANDRE_ÅRSAKER) {
            validateNotNull("Årsakstekst", årsakstekst, feil);
        }
        if (yrkeskategorier.isEmpty()) {
            feil.add("Yrkeskategori");
        }
        if (feil.isEmpty()) {
            return;
        }
        throw new AlleFelterIkkeFyltUtException(feil);
    }

    private void validateNotNull(String desc, Object object, List<String> feil) {
        if (ObjectUtils.isEmpty(object)) {
            LOG.warn("Validering feilet, er null {}", desc);
            feil.add(desc);
        }
    }

    private void sjekkOmSkjemaErAvbrutt() {
        if (avbrutt) {
            throw new SkjemaErAvbruttException();
        }
    }

    public void avbryt(String utførtAv) {
        sjekkOmSkjemaErAvbrutt();
        sjekkOmSkjemaErSendtInn();
        setAvbrutt(true);
        registerEvent(new PermitteringsskjemaAvbrutt(this, utførtAv));
    }
}
