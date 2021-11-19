package no.nav.permitteringsskjemaapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = { "spring.cloud.vault.enabled=false" })
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
