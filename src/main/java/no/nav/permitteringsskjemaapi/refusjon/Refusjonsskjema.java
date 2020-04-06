package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaAvbrutt;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaEndret;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaSendtInn;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
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
        registerEvent(new RefusjonsskjemaEndret(this, fnr));
    }

    public void sendInn(String fnr) {
        registerEvent(new RefusjonsskjemaSendtInn(this, fnr));
    }

    public void avbryt(String fnr) {
        registerEvent(new RefusjonsskjemaAvbrutt(this, fnr));
    }
}
