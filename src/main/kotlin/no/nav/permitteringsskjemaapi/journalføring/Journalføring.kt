package no.nav.permitteringsskjemaapi.journalføring

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "journalforing")
class Journalføring {
    enum class State {
        NY,
        JOURNALFORT,
        FERDIG;
    }

    @field:Id
    @field:Column(name = "skjema_id")
    lateinit var skjemaid: UUID

    @field:Column(name = "state")
    @field:Enumerated(EnumType.STRING)
    private lateinit var persistedState: State

    @field:Column(name = "row_inserted_at")
    lateinit var rowInsertedAt: String

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

    @PostLoad
    fun validatePersistedState() {
        check(persistedState == state)
    }

    @PrePersist
    @PreUpdate
    fun updatePersistedState() {
        persistedState = state
    }

    val state: State
        get() = when {
            journalført == null && oppgave == null -> State.NY
            journalført != null && oppgave == null -> State.JOURNALFORT
            journalført != null && oppgave != null -> State.FERDIG
            else -> error("oppgave opprettet, men journalføring ikke registrert")
        }

    override fun equals(other: Any?) =
        this === other || (other is Journalføring && this.skjemaid == other.skjemaid)

    override fun hashCode() = skjemaid.hashCode()
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
}

@Embeddable
class Journalført() {
    constructor(
        journalpostId: String,
        journalfortAt: String,
        kommunenummer: String,
        behandlendeEnhet: String
    ): this() {
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
    lateinit var kommunenummer: String
        private set

    @field:Column(name = "behandlende_enhet")
    lateinit var behandlendeEnhet: String
        private set
}