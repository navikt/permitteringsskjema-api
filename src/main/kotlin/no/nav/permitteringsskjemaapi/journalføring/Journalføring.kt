package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "journalforing")
class Journalføring() {
    constructor(skjemaid: UUID) : this() {
        this.skjemaid = skjemaid
        this.rowInsertedAt = Instant.now().toString()
        this.state = State.NY
    }

    enum class State {
        NY,
        JOURNALFORT,
        FERDIG;
    }

    @field:Id
    @field:Column(name = "skjema_id")
    lateinit var skjemaid: UUID
        private set

    @field:Column(name = "state")
    @field:Enumerated(EnumType.STRING)
    lateinit var state: State

    @field:Column(name = "row_inserted_at")
    lateinit var rowInsertedAt: String
        private set

    @field:Embedded
    var journalført: Journalført? = null
        set(newValue) {
            check(journalført === null)
            field = newValue
        }

    @field:Embedded
    var oppgave: Oppgave? = null
        set(newValue) {
            check(oppgave === null)
            field = newValue
        }

    override fun equals(other: Any?) =
        this === other || (other is Journalføring && this.skjemaid == other.skjemaid)

    override fun hashCode() = skjemaid.hashCode()
    override fun toString() =
        "Journalføring(skjemaid=$skjemaid, state=$state, rowInsertedAt='$rowInsertedAt', journalført=$journalført, oppgave=$oppgave)"
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