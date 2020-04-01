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
public class RefusjonsskjemaRepositoryTest {
    private Refusjonsskjema lagretRefusjonsskjema;
    private Refusjonsskjema refusjonsskjema;

    @Autowired
    private RefusjonsskjemaRepository repository;

    @Before
    public void setUp() {
        refusjonsskjema = RefusjonTestData.enRefusjonMedAltFyltUt();
        lagretRefusjonsskjema = repository.save(refusjonsskjema);
    }

    @Test
    public void skal_kunne_lagre_alle_felter() {
        assertThat(lagretRefusjonsskjema)
                .usingRecursiveComparison() // Felt-for-felt sammenligning
                .isEqualTo(refusjonsskjema);
    }

    @Test
    public void skal_kunne_hentes_med_id() {
        Optional<Refusjonsskjema> hentetRefusjon = repository.findById(refusjonsskjema.getId());
        assertThat(hentetRefusjon).hasValue(refusjonsskjema);
    }
}
