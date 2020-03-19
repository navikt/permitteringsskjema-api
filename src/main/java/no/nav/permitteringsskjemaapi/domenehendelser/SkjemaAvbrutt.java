package no.nav.permitteringsskjemaapi.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.Permitteringsskjema;

@Value
public class SkjemaAvbrutt {
    Permitteringsskjema permitteringsskjema;
    String utførtAv;
}
