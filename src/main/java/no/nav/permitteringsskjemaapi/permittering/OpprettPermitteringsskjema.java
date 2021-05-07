package no.nav.permitteringsskjemaapi.permittering;

import lombok.Value;

import java.util.List;

@Value
public class OpprettPermitteringsskjema {
    String bedriftNr;
    PermitteringsskjemaType type;
    List<String> underenheter;
}
