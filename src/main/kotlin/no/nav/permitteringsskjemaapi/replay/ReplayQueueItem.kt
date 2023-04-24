package no.nav.permitteringsskjemaapi.replay

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "replay_queue_items")
data class ReplayQueueItem(
    @field:Id
    var id: UUID? = null,

    @field:Column(name = "skjema_id")
    var skjemaId: UUID? = null,
)