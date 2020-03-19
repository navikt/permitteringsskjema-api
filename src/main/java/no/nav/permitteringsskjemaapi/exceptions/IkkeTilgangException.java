package no.nav.permitteringsskjemaapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class IkkeTilgangException extends RuntimeException {
}
