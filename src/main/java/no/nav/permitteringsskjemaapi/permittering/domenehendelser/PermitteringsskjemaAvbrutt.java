package no.nav.permitteringsskjemaapi.permittering.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;

@Value
public class PermitteringsskjemaAvbrutt {
    Permitteringsskjema permitteringsskjema;
    String utførtAv;
}
