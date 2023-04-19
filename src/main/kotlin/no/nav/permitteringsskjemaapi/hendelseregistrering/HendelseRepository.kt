package no.nav.permitteringsskjemaapi.hendelseregistrering

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface HendelseRepository : JpaRepository<Hendelse?, UUID?>