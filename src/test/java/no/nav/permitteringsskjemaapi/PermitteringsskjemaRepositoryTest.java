package no.nav.permitteringsskjemaapi;

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaJuridiskEnhet;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaJuridiskEnhetRepository;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("test")
@DirtiesContext
public class PermitteringsskjemaRepositoryTest {
    private Permitteringsskjema lagretPermitteringsskjema;
    private Permitteringsskjema permitteringsskjema;

    private PermitteringsskjemaJuridiskEnhet permitteringsskjemaJuridiskEnhet;
    private PermitteringsskjemaJuridiskEnhet lagretPermitteringsskjemaJuridiskEnhet;

    @Autowired
    private PermitteringsskjemaRepository repository;
    @Autowired
    private PermitteringsskjemaJuridiskEnhetRepository juridiskEnhetRepository;

    @Before
    public void setUp() {
        permitteringsskjema = PermitteringTestData.enPermitteringMedAltFyltUt();
        permitteringsskjemaJuridiskEnhet = PermitteringTestData.enJuridiskEnhetUtenSkjemaer();

        lagretPermitteringsskjema = repository.save(permitteringsskjema);
        lagretPermitteringsskjemaJuridiskEnhet = juridiskEnhetRepository.save(permitteringsskjemaJuridiskEnhet);
    }

    @Test
    public void skal_kunne_lagre_alle_felter() {
        assertThat(lagretPermitteringsskjema)
                .usingRecursiveComparison() // Felt-for-felt sammenligning
                .isEqualTo(permitteringsskjema);
        assertThat(lagretPermitteringsskjemaJuridiskEnhet)
                .usingRecursiveComparison()
                .isEqualTo(permitteringsskjemaJuridiskEnhet);
    }

    @Test
    public void skal_lagre_og_hente_juridisk_enhet_med_liste_av_underenheter() {
        PermitteringsskjemaJuridiskEnhet permitteringsskjemaJuridiskSkalHaEnOrganisasjon = PermitteringTestData.enJuridiskEnhetUtenSkjemaer();
        Permitteringsskjema permitteringsskjemaOrganisasjon = PermitteringTestData.enPermitteringMedAltFyltUt();
        permitteringsskjemaOrganisasjon.setPermitteringsskjemaJuridiskEnhet(permitteringsskjemaJuridiskSkalHaEnOrganisasjon);
        permitteringsskjemaJuridiskSkalHaEnOrganisasjon.setPermitteringsskjemaer(List.of(permitteringsskjemaOrganisasjon));
        PermitteringsskjemaJuridiskEnhet juridiskEnhetMedListeAvOrganisasjoner = juridiskEnhetRepository.save(permitteringsskjemaJuridiskSkalHaEnOrganisasjon);

        assertThat(juridiskEnhetMedListeAvOrganisasjoner.getPermitteringsskjemaer()).isNotEmpty();
    }

    @Test
    public void skal_kunne_hentes_med_id() {
        Optional<Permitteringsskjema> hentetPermittering = repository.findById(permitteringsskjema.getId());
        assertThat(hentetPermittering).hasValue(permitteringsskjema);
    }
}
