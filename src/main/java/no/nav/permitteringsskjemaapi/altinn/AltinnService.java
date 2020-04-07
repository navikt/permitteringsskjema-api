package no.nav.permitteringsskjemaapi.altinn;

import java.util.List;

public interface AltinnService {
    List<AltinnOrganisasjon> hentOrganisasjoner(String fnr);
    List<AltinnOrganisasjon> hentOrganisasjonerBasertPaRettigheter(String fnr, String serviceKode, String serviceEdition);
}
