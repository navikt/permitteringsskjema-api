package no.nav.permitteringsskjemaapi.exceptions;

public class AlleFelterIkkeFyltUtException extends RuntimeException {
    public AlleFelterIkkeFyltUtException() {
        super("Alle felter er ikke fylt ut");
    }
}
