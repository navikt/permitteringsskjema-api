package no.nav.permitteringsskjemaapi.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

class AlleFelterIkkeFyltUtException(feil: List<String>) : RuntimeException("Alle felter er ikke fylt ut $feil")

class IkkeFunnetException : RuntimeException()

class IkkeTilgangException : RuntimeException()

class SkjemaErAvbruttException : RuntimeException("Skjema er avbrutt")

class StartdatoPassertException : RuntimeException("Skjema kan ikke trekkes etter startdato")

class AlleredeTrukketException : RuntimeException("Skjema er allerede trukket")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class PermitteringsApiException(message: String?) : RuntimeException(message)

inline fun <reified T : Throwable> Throwable.isCausedBy() = findCause<T>() != null

inline fun <reified T : Throwable> Throwable.findCause(): T? {
    var current: Throwable? = this
    var level = 0
    while (current != null) {
        if (current is T) {
            return current
        }
        if (level > 100) {
            // avoid stack overflow due to circular references
            return null
        }
        current = current.cause
        level += 1
    }
    return null
}