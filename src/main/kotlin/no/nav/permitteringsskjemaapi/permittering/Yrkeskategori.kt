package no.nav.permitteringsskjemaapi.permittering

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.*

@Entity
data class Yrkeskategori(
    @JsonIgnore
    @field:Id
    var id: UUID? = null,
    @JsonIgnore
    @field:ManyToOne
    @field:JoinColumn(name = "permitteringsskjema_id")
    var permitteringsskjema: Permitteringsskjema? = null,
    var konseptId: Int? = null,
    var styrk08: String? = null,
    var label: String? = null,
    var antall: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Yrkeskategori

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}