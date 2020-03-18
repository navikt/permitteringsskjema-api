package no.nav.permitteringsskjemaapi.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnLocal
public class FakeAltinnService implements AltinnService {
    @Override
    public List<AltinnOrganisasjon> hentOrganisasjoner(String fnr) {
        return List.of(new AltinnOrganisasjon("STORFOSNA OG FREDRIKSTAD REGNSKAP", "Business", "910825569", "BEDR", "Active", "910825550"));
    }
}