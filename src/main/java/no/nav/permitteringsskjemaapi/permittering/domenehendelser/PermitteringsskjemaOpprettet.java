package no.nav.permitteringsskjemaapi.permittering.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;

@Value
public class PermitteringsskjemaOpprettet {
    Permitteringsskjema permitteringsskjema;
    String utførtAv;
}
