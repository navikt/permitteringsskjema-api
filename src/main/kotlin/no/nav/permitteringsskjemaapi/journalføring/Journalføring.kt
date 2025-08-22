package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.*
import no.nav.permitteringsskjemaapi.permittering.HendelseType
import java.time.Instant
import java.util.*

@Entity
@Table(name = "journalforing")
class Journalføring() {
    constructor(skjemaid: UUID, hendelseType: HendelseType) : this() {
        this.skjemaid = skjemaid
        this.rowInsertedAt = Instant.now().toString()
        // TRUKKET skal kun journalføres, ingen oppgave -> bruk NEEDS_JOURNALFORING_ONLY
        this.state =  if (hendelseType == HendelseType.TRUKKET) State.NEEDS_JOURNALFORING_ONLY else State.NY
        this.hendelseType = hendelseType
    }

    enum class State {
        NY,
        JOURNALFORT,
        FERDIG,
        NEEDS_JOURNALFORING_ONLY;
    }

    @field:Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id")
    lateinit var id: UUID
        private set


    @field:Column(name = "skjema_id")
    lateinit var skjemaid: UUID

    @field:Column(name = "hendelse_type")
    @field:Enumerated(EnumType.STRING)
    var hendelseType: HendelseType = HendelseType.INNSENDT

    @field:Column(name = "state")
    @field:Enumerated(EnumType.STRING)
    lateinit var state: State

    @field:Column(name = "row_inserted_at")
    lateinit var rowInsertedAt: String
        private set

    @field:Embedded
    var journalført: Journalført? = null
        set(newValue) {
            check(field === null)
            field = newValue
        }

    @field:Embedded
    var oppgave: Oppgave? = null
        set(newValue) {
            check(field === null)
            field = newValue
        }

    @field:Column(name = "delayed_until")
    @field:Convert(converter = InstantAsIsoStringConverter::class)
    var delayedUntil: Instant? = null

    override fun toString() =
        "Journalføring(id=$id, skjemaid=$skjemaid, hendelseType=$hendelseType, state=$state, rowInsertedAt='$rowInsertedAt', delayedUntil='$delayedUntil', journalført=$journalført, oppgave=$oppgave)"
}

@Embeddable
class Oppgave() {
    constructor(oppgaveId: String, oppgaveOpprettetAt: String) : this() {
        this.oppgaveId = oppgaveId
        this.oppgaveOpprettetAt = oppgaveOpprettetAt
    }

    @field:Column(name = "oppgave_id")
    lateinit var oppgaveId: String
        private set

    @field:Column(name = "oppgave_opprettet_at")
    lateinit var oppgaveOpprettetAt: String
        private set

    override fun toString() = "Oppgave(oppgaveId='$oppgaveId', oppgaveOpprettetAt='$oppgaveOpprettetAt')"
}

@Embeddable
class Journalført() {
    constructor(
        journalpostId: String,
        journalfortAt: String,
        kommunenummer: String?,
        behandlendeEnhet: String
    ) : this() {
        this.journalpostId = journalpostId
        this.journalfortAt = journalfortAt
        this.kommunenummer = kommunenummer
        this.behandlendeEnhet = behandlendeEnhet
    }

    @field:Column(name = "journalpost_id")
    lateinit var journalpostId: String
        private set

    @field:Column(name = "journalfort_at")
    lateinit var journalfortAt: String
        private set

    @field:Column(name = "kommunenummer")
    var kommunenummer: String? = null
        private set

    @field:Column(name = "behandlende_enhet")
    lateinit var behandlendeEnhet: String
        private set

    override fun toString() =
        "Journalført(journalpostId='$journalpostId', journalfortAt='$journalfortAt', kommunenummer='$kommunenummer', behandlendeEnhet='$behandlendeEnhet')"
}

class InstantAsIsoStringConverter : AttributeConverter<Instant?, String?> {
    override fun convertToDatabaseColumn(attribute: Instant?): String? = attribute?.toString()
    override fun convertToEntityAttribute(dbData: String?): Instant? = dbData?.let { Instant.parse(it) }
}