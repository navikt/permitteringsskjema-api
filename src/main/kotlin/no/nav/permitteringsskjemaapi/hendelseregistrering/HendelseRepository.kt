package no.nav.permitteringsskjemaapi.hendelseregistrering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HendelseRepository extends JpaRepository<Hendelse, UUID> {
}
