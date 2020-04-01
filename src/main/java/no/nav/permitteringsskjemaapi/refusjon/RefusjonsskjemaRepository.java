package no.nav.permitteringsskjemaapi.refusjon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefusjonsskjemaRepository extends JpaRepository<Refusjonsskjema, UUID> {
    Optional<Refusjonsskjema> findByIdAndOpprettetAv(UUID id, String opprettetAv);
    List<Refusjonsskjema> findAllByOpprettetAv(String opprettetAv);
}
