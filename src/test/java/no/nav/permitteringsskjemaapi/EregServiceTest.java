package no.nav.permitteringsskjemaapi;

import no.nav.permitteringsskjemaapi.ereg.EregOrganisasjon;
import no.nav.permitteringsskjemaapi.ereg.EregService;
import no.nav.permitteringsskjemaapi.exceptions.EnhetFinnesIkkeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"wiremock","local"})
public class EregServiceTest {
    @Autowired
    private EregService eregService;

    @Test
    public void hentBedriftNavn__returnerer_navn_og_bedriftnr() {
        EregOrganisasjon organisasjon = eregService.hentOrganisasjon ("910825550");
        assertThat(organisasjon.getOrganisasjonsnummer()).isEqualTo("910825550");
        assertThat(organisasjon.hentNavn()).isEqualTo("Tranøy og Sande i Vestfold Regnskap");
    }

    @Test(expected = EnhetFinnesIkkeException.class)
    public void hentBedriftNavn__kaster_exception_ved_404() {
        eregService.hentOrganisasjon("899999999");
    }
}
