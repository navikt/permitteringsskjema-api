package no.nav.permitteringsskjemaapi;

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
public class PermitteringRepositoryTest {

    @Autowired
    private PermitteringRepository repository;

    @Test
    public void ny_permittering_skal_kunne_lagres_og_hentes_av_repository() {
        Permittering lagretPermittering = repository.save(TestData.enPermittering());

        Optional<Permittering> hentetPermittering = repository.findById(lagretPermittering.getId());
        assertThat(hentetPermittering).isPresent();
    }
}
