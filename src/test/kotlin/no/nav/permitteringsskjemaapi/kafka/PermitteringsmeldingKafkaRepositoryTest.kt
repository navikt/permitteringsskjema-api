package no.nav.permitteringsskjemaapi.kafka

import jakarta.transaction.Transactional
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class PermitteringsmeldingKafkaRepositoryTest {
    @Autowired
    lateinit var permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    @Transactional
    fun writerOrderPreserved() {
        val id1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val id2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001")
        val id3 = UUID.fromString("123e4567-e89b-12d3-a456-426614174002")

        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(id1))
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(id2))
        permitteringsmeldingKafkaRepository.save(PermitteringsmeldingKafkaEntry(id3))

        val result = permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(10))
        assertEquals(listOf(id1, id2, id3), result.map { it.skjemaId } )
    }
}