package no.nav.permitteringsskjemaapi.altinn;

import java.util.List;

public interface AltinnService {
    List<AltinnOrganisasjon> hentOrganisasjoner(String fnr);
    List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter(String fnr, String serviceKode, String serviceEdition);
}
