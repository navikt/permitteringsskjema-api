package no.nav.permitteringsskjemaapi.refusjon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaAvbrutt;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaEndret;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaSendtInn;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Refusjonsskjema extends AbstractAggregateRoot {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private Instant opprettetTidspunkt;
    private Instant sendtInnTidspunkt;
    private String opprettetAv;
    private String bedriftNr;
    private String bedriftNavn;
    private String kontaktEpost;
    private String kontaktNavn;
    private String kontaktTlf;
    private boolean avbrutt;

    @OneToMany(mappedBy = "refusjonsskjema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @JsonIgnore
    private List<Arbeidsforhold> arbeidsforhold = new ArrayList<>();

    public static Refusjonsskjema opprett(OpprettRefusjonsskjema opprettSkjema, String fnr) {
        Refusjonsskjema refusjonsskjema = new Refusjonsskjema();
        refusjonsskjema.setId(UUID.randomUUID());
        refusjonsskjema.setOpprettetTidspunkt(Instant.now());
        refusjonsskjema.setOpprettetAv(fnr);
        refusjonsskjema.setBedriftNr(opprettSkjema.getBedriftNr());
        return refusjonsskjema;
    }

    public void endre(EndreRefusjonsskjema endreRefusjon, String fnr) {
        setKontaktEpost(endreRefusjon.getKontaktEpost());
        setKontaktTlf(endreRefusjon.getKontaktTlf());
        setKontaktNavn(endreRefusjon.getKontaktNavn());
        arbeidsforhold.clear();
        arbeidsforhold.addAll(endreRefusjon.getArbeidsforhold());
        arbeidsforhold.forEach(y -> y.setId(UUID.randomUUID()));
        arbeidsforhold.forEach(y -> y.setRefusjonsskjema(this));
        registerEvent(new RefusjonsskjemaEndret(this, fnr));
    }

    public void sendInn(String fnr) {
        registerEvent(new RefusjonsskjemaSendtInn(this, fnr));
    }

    public void avbryt(String fnr) {
        registerEvent(new RefusjonsskjemaAvbrutt(this, fnr));
    }
}
