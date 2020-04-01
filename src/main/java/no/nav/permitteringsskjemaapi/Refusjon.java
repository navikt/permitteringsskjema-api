package no.nav.permitteringsskjemaapi;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Refusjon extends AbstractAggregateRoot {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private Instant opprettetTidspunkt;
    private String opprettetAv;
    private String bedriftNr;
    private String bedriftNavn;

    public static Refusjon opprett(OpprettRefusjon opprettSkjema, String fnr) {
        Refusjon refusjon = new Refusjon();
        refusjon.setId(UUID.randomUUID());
        refusjon.setOpprettetTidspunkt(Instant.now());
        refusjon.setOpprettetAv(fnr);
        refusjon.setBedriftNr(opprettSkjema.getBedriftNr());
        return refusjon;
    }

    public void endre(EndreRefusjon endreRefusjon, String fnr) {

    }

    public void sendInn(String fnr) {

    }

    public void avbryt(String fnr) {

    }
}
