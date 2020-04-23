package no.nav.permitteringsskjemaapi.integrasjonMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ConditionalOnLocal
@Slf4j
@Component
public class IntegrasjonerMockServer implements DisposableBean {

    private final WireMockServer server;

    @Autowired
    public IntegrasjonerMockServer(  @Value("${mock.port}") int port) {
        log.info("Starter mockserver for eksterne integrasjoner. {}", port);
        server = new WireMockServer(WireMockConfiguration.options().usingFilesUnderClasspath("src/main/resources").port(port));
        server.start();
    }


    @Override
    public void destroy() {
        log.info("Stopper mockserver.");
        server.stop();
    }
}
