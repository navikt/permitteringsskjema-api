package no.nav.permitteringsskjemaapi.journalføring

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.jvm.optionals.getOrElse

@Service
class JournalføringMetrics(
    meterRegistry: MeterRegistry,
    private val journalføringRepository: JournalføringRepository,
) {
    private val oldestSeconds = AtomicLong(0)

    init {
        meterRegistry.gauge(
            "permitteringsskjema.journalforing.oldest.seconds",
            oldestSeconds
        )
    }


    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    fun loop() {
        val seconds = journalføringRepository
            .oldestInsertionTimeNotCompleted().map {
                Duration.between(Instant.now(), Instant.parse(it))
                    .abs()
                    .toSeconds()
            }
            .getOrElse { 0 }
        oldestSeconds.set(seconds)
    }
}