package no.nav.permitteringsskjemaapi.exceptions;

public class SkjemaErAvbruttException extends RuntimeException {
    public SkjemaErAvbruttException() {
        super("Skjema er avbrutt");
    }
}
