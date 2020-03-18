package no.nav.permitteringsskjemaapi.hendelseregistrering;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class Hendelse {
    @Id
    private UUID id;
    private UUID skjemaId;
    private Instant tidspunkt;
    @Enumerated(EnumType.STRING)
    private HendelseType type;
    private String utførtAv;

    public static Hendelse nyHendelse(UUID skjemaId, HendelseType type, String utførtAv) {
        Hendelse hendelse = new Hendelse();
        hendelse.id = UUID.randomUUID();
        hendelse.skjemaId = skjemaId;
        hendelse.tidspunkt = Instant.now();
        hendelse.type = type;
        hendelse.utførtAv = utførtAv;
        return hendelse;
    }
}
