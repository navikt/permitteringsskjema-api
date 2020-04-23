package no.nav.permitteringsskjemaapi.ereg;

import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnLocal
@Profile("!wiremock")
public class FakeEregServiceImpl implements EregService {
    @Override
    public EregOrganisasjon hentOrganisasjon(String orgnr) {
        return new EregOrganisasjon("910825550",new EnhetsRegisterNavn("BAREKSTAD OG",null,null,"Yttrevåg Regnskap") );
    }
}
