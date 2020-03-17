package no.nav.permitteringsskjemaapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PermitteringsApiException extends RuntimeException {

    public PermitteringsApiException(String message) {
        super(message);
    }

    public PermitteringsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
