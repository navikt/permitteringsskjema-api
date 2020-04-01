package no.nav.permitteringsskjemaapi.refusjon.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.refusjon.Refusjonsskjema;

@Value
public class RefusjonsskjemaAvbrutt {
    Refusjonsskjema refusjonsskjema;
    String utførtAv;
}
