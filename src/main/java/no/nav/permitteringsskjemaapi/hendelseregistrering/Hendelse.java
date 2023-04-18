package no.nav.permitteringsskjemaapi.hendelseregistrering;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

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
