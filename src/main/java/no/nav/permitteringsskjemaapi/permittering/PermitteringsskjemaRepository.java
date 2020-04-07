package no.nav.permitteringsskjemaapi.permittering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermitteringsskjemaRepository extends JpaRepository<Permitteringsskjema, UUID> {
    Optional<Permitteringsskjema> findByIdAndOpprettetAv(UUID id, String opprettetAv);
    List<Permitteringsskjema> findAllByOpprettetAv(String opprettetAv);
}