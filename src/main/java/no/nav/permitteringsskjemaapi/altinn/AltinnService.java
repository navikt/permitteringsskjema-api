package no.nav.permitteringsskjemaapi.altinn;

import java.util.List;

public interface AltinnService {
    List<AltinnOrganisasjon> hentOrganisasjoner();
    List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter(String serviceKode, String serviceEdition);
}
