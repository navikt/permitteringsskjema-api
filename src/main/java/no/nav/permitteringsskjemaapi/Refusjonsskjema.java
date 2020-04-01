package no.nav.permitteringsskjemaapi;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.Instant;
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
    private List<Arbeidsforhold> arbeidsforhold;

    public static Refusjonsskjema opprett(OpprettRefusjon opprettSkjema, String fnr) {
        Refusjonsskjema refusjonsskjema = new Refusjonsskjema();
        refusjonsskjema.setId(UUID.randomUUID());
        refusjonsskjema.setOpprettetTidspunkt(Instant.now());
        refusjonsskjema.setOpprettetAv(fnr);
        refusjonsskjema.setBedriftNr(opprettSkjema.getBedriftNr());
        return refusjonsskjema;
    }

    public void endre(EndreRefusjon endreRefusjon, String fnr) {

    }

    public void sendInn(String fnr) {

    }

    public void avbryt(String fnr) {

    }
}
