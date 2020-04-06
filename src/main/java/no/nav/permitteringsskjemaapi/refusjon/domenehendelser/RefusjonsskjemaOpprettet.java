package no.nav.permitteringsskjemaapi.refusjon.domenehendelser;

import lombok.Value;
import no.nav.permitteringsskjemaapi.refusjon.Refusjonsskjema;

@Value
public class RefusjonsskjemaOpprettet {
    Refusjonsskjema refusjonsskjema;
    String utførtAv;
}
