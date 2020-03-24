package no.nav.permitteringsskjemaapi.exceptions;

public class IkkeAltFyltUtException extends RuntimeException {
    public IkkeAltFyltUtException() {
        super("Ikke alt er fylt ut");
    }
}
