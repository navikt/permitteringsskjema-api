package no.nav.permitteringsskjemaapi.permittering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermitteringsskjemaJuridiskEnhetRepository extends JpaRepository<PermitteringsskjemaJuridiskEnhet, UUID> {
    Optional<PermitteringsskjemaJuridiskEnhet> findByIdAndOpprettetAv(UUID id, String opprettetAv);
    List<PermitteringsskjemaJuridiskEnhet> findAllByOpprettetAv(String opprettetAv);
}
