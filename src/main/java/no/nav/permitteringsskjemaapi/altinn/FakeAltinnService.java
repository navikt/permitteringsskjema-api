package no.nav.permitteringsskjemaapi.altinn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.permitteringsskjemaapi.config.Constants.DEFAULT;
import static no.nav.permitteringsskjemaapi.config.Constants.LOCAL;

@Slf4j
@Component
@Profile({DEFAULT, LOCAL})
public class FakeAltinnService implements AltinnService {
    @Override
    public List<AltinnOrganisasjon> hentOrganisasjoner(String fnr) {
        return List.of(new AltinnOrganisasjon("STORFOSNA OG FREDRIKSTAD REGNSKAP", "Business", "910825569", "BEDR", "Active", "910825550"),
                new AltinnOrganisasjon("BALLSTAD OG HORTEN", "Enterprise", "910825550", "AS", "Active", null),
                new AltinnOrganisasjon("BAREKSTAD OG YTTERVÅG REGNSKAP", "Enterprise", "910998250", "AS", "Active", null),
                new AltinnOrganisasjon("BALLSTAD OG HORTEN", "Business", "810514442", "BEDR", "Active", "910998250"));

    }

    @Override
    public List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter(String fnr, String serviceKode, String serviceEdition) {
        return List.of(new AltinnOrganisasjon("SALTRØD OG HØNSEBY", "Business", "910825569", "BEDR", "Active", "910825550"),
                new AltinnOrganisasjon("BALLSTAD OG HORTEN", "Enterprise", "910825550", "AS", "Active", null),
                new AltinnOrganisasjon("BAREKSTAD OG YTTERVÅG REGNSKAP", "Enterprise", "910998250", "AS", "Active", null),
                new AltinnOrganisasjon("BALLSTAD OG HORTEN", "Business", "810514442", "BEDR", "Active", "910998250"));
    }
}
