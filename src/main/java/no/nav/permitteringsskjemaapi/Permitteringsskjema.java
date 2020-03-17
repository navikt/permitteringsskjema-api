package no.nav.permitteringsskjemaapi;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaEndret;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaOpprettet;
import no.nav.permitteringsskjemaapi.domenehendelser.SkjemaSendtInn;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Lombok
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    private Instant opprettetTidspunkt;
    private String orgNr;
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

    public static Permitteringsskjema nyttSkjema(String orgNr) {
        Permitteringsskjema skjema = new Permitteringsskjema();
        skjema.setId(UUID.randomUUID());
        skjema.setOpprettetTidspunkt(Instant.now());
        skjema.setOrgNr(orgNr);
        skjema.registerEvent(new SkjemaOpprettet(skjema));
        return skjema;
    }

    private static boolean isAnyEmpty(Object... objects) {
        for (Object object : objects) {
            if (ObjectUtils.isEmpty(object)) {
                return true;
            }
        }
        return false;
    }

    public void endre(EndreSkjema endreSkjema) {
        sjekkOmSkjemaErSendtInn();
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
        if (isAnyEmpty(
                type,
                kontaktNavn,
                kontaktTlf,
                varsletAnsattDato,
                varsletNavDato,
                startDato,
                sluttDato,
                ukjentSluttDato,
                fritekst)
        ) {
            throw new RuntimeException("Alle felter er ikke fylt ut");
        }
    }
}
