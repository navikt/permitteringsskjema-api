package no.nav.permitteringsskjemaapi.hendelseregistrering

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
data class Hendelse(
    @field:Id
    var id: UUID? = null,
    var skjemaId: UUID? = null,
    var tidspunkt: Instant? = null,
    @field:Enumerated(EnumType.STRING)
    var type: HendelseType? = null,
    var utførtAv: String? = null,

) {
    companion object {
        fun nyHendelse(skjemaId: UUID, type: HendelseType, utførtAv: String): Hendelse {
            val hendelse = Hendelse()
            hendelse.id = UUID.randomUUID()
            hendelse.skjemaId = skjemaId
            hendelse.tidspunkt = Instant.now()
            hendelse.type = type
            hendelse.utførtAv = utførtAv
            return hendelse
        }
    }
}