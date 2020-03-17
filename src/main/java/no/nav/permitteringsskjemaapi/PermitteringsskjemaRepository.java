package no.nav.permitteringsskjemaapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermitteringsskjemaRepository extends JpaRepository<Permitteringsskjema, UUID> {

}
