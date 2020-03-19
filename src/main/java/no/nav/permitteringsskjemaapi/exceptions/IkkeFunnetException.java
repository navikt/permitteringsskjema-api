package no.nav.permitteringsskjemaapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IkkeFunnetException extends RuntimeException {
}
