package no.nav.permitteringsskjemaapi.exceptions;

import java.util.List;

public class AlleFelterIkkeFyltUtException extends RuntimeException {
    public AlleFelterIkkeFyltUtException(List<String> feil) {
        super("Alle felter er ikke fylt ut " + feil);
    }
}
