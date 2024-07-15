package no.nav.permitteringsskjemaapi

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun timeline(step: Duration = 10.seconds): List<Instant> =
    generateSequence(Instant.now()) { it + step.toJavaDuration() }
        .take(10)
        .toList()