package no.nav.permitteringsskjemaapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PermitteringsskjemaRepository extends JpaRepository<Permitteringsskjema, UUID> {
    Permitteringsskjema findByIdAndOrgNr(UUID id, String orgNr);
    List<Permitteringsskjema> findAllByOrgNr(String orgNr);
}
