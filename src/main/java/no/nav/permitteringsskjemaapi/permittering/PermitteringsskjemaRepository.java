package no.nav.permitteringsskjemaapi.permittering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermitteringsskjemaRepository extends JpaRepository<Permitteringsskjema, UUID> {
    Optional<Permitteringsskjema> findByIdAndOpprettetAv(UUID id, String opprettetAv);
    Optional<Permitteringsskjema> findById(UUID id);
    List<Permitteringsskjema> findAllByOpprettetAv(String opprettetAv);
    List<Permitteringsskjema> findAllByBedriftNr(String bedriftNr);
}
