package no.nav.permitteringsskjemaapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class IkkeTilgangException extends RuntimeException {
}
