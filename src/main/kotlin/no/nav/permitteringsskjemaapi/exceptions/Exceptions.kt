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