package no.nav.permitteringsskjemaapi.refusjon.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.refusjon.Refusjonsskjema;

@Value
public class RefusjonsskjemaSendtInn {
    Refusjonsskjema refusjonsskjema;
    String utførtAv;
}
