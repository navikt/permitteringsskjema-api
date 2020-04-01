package no.nav.permitteringsskjemaapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefusjonRepository extends JpaRepository<Refusjon, UUID> {
    Optional<Refusjon> findByIdAndOpprettetAv(UUID id, String opprettetAv);
    List<Refusjon> findAllByOpprettetAv(String opprettetAv);
}
