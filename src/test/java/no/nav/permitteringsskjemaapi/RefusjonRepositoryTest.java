package no.nav.permitteringsskjemaapi;

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
public class RefusjonRepositoryTest {
    private Refusjon lagretRefusjon;
    private Refusjon refusjon;

    @Autowired
    private RefusjonRepository repository;

    @Before
    public void setUp() {
        refusjon = RefusjonTestData.enRefusjonMedAltFyltUt();
        lagretRefusjon = repository.save(refusjon);
    }

    @Test
    public void skal_kunne_lagre_alle_felter() {
        assertThat(lagretRefusjon)
                .usingRecursiveComparison() // Felt-for-felt sammenligning
                .isEqualTo(refusjon);
    }

    @Test
    public void skal_kunne_hentes_med_id() {
        Optional<Refusjon> hentetRefusjon = repository.findById(refusjon.getId());
        assertThat(hentetRefusjon).hasValue(refusjon);
    }
}
