package no.nav.permitteringsskjemaapi.permittering

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PermitteringsskjemaRepository : JpaRepository<Permitteringsskjema?, UUID> {
    fun findByIdAndOpprettetAv(id: UUID, opprettetAv: String): Optional<Permitteringsskjema>
    override fun findById(id: UUID): Optional<Permitteringsskjema?>
    fun findAllByOpprettetAv(opprettetAv: String): List<Permitteringsskjema>
    fun findAllByBedriftNr(bedriftNr: String): List<Permitteringsskjema>
}