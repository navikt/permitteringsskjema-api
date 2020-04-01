package no.nav.permitteringsskjemaapi;

import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema;
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
        permitteringsskjema = PermitteringTestData.enPermitteringMedAltFyltUt();
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
}
