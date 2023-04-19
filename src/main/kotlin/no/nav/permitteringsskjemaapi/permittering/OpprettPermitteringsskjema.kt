package no.nav.permitteringsskjemaapi.permittering;

import lombok.Value;

@Value
public class OpprettPermitteringsskjema {
    String bedriftNr;
    PermitteringsskjemaType type;
}
