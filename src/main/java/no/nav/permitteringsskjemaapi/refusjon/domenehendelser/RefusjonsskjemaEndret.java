package no.nav.permitteringsskjemaapi.refusjon.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.refusjon.Refusjonsskjema;

@Value
public class RefusjonsskjemaEndret {
    Refusjonsskjema refusjonsskjema;
    String utførtAv;
}
