package no.nav.permitteringsskjemaapi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
public class PermitteringsskjemaRepositoryTest {
    private Permitteringsskjema lagretPermitteringsskjema;
    private Permitteringsskjema permitteringsskjema;

    @Autowired
    private PermitteringsskjemaRepository repository;

    @Before
    public void setUp() {
        permitteringsskjema = TestData.enPermittering();
        lagretPermitteringsskjema = repository.save(permitteringsskjema);
    }

    @Test
    public void skal_kunne_lagre_alle_felter() {
        assertThat(lagretPermitteringsskjema)
                .usingRecursiveComparison() // Felt-for-felt sammenligning
                .isEqualTo(permitteringsskjema);
    }

    @Test
    public void skal_kunne_hentes_med_id() {
        Optional<Permitteringsskjema> hentetPermittering = repository.findById(permitteringsskjema.getId());
        assertThat(hentetPermittering).hasValue(permitteringsskjema);
    }

    @Test
    public void skal_kunne_hentes_med_orgnr() {
        List<Permitteringsskjema> hentetPermittering = repository.findAllByOrgNr(permitteringsskjema.getOrgNr());
        assertThat(hentetPermittering).contains(permitteringsskjema);
    }

    @Test
    public void skal_ikke_kunne_hentes_med_annet_orgnr() {
        List<Permitteringsskjema> hentetPermittering = repository.findAllByOrgNr("000000000");
        assertThat(hentetPermittering).doesNotContain(permitteringsskjema);
    }
}
