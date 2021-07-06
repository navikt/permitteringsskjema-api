package no.nav.permitteringsskjemaapi.permittering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.permitteringsskjemaapi.permittering.domenehendelser.PermitteringsskjemaOpprettet;
import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class PermitteringsskjemaJuridiskEnhet {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String bedriftNr;
    private String bedriftNavn;
    @Enumerated(EnumType.STRING)
    private PermitteringsskjemaType type;
    private Instant opprettetTidspunkt;
    @JsonIgnore
    private String opprettetAv;
    @OneToMany
    private List<Permitteringsskjema> permitteringsskjemaer;

    public static PermitteringsskjemaJuridiskEnhet opprettSkjema(OpprettPermitteringsskjema opprettSkjema, String utførtAv) {
        PermitteringsskjemaJuridiskEnhet skjemaJuridiskEnhet = new PermitteringsskjemaJuridiskEnhet();
        skjemaJuridiskEnhet.setId(UUID.randomUUID());
        skjemaJuridiskEnhet.setOpprettetTidspunkt(Instant.now());
        skjemaJuridiskEnhet.setOpprettetAv(utførtAv);
        skjemaJuridiskEnhet.setBedriftNr(opprettSkjema.getBedriftNr());
        return skjemaJuridiskEnhet;
    }


    public void avbryt(String autentisertBruker) {
        throw new NotImplementedException("Må kodes da");
    }

    public void sendInn(String autentisertBruker) {
        throw new NotImplementedException("Må kodes da");
    }
}
