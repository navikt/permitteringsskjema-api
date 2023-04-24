package no.nav.permitteringsskjemaapi.replay;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "replay_queue_items")
public class ReplayQueueItem {
    @Id
    UUID id;

    @Column(name = "skjema_id")
    UUID skjemaId;
}
