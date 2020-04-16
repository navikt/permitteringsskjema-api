package no.nav.permitteringsskjemaapi.refusjon;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "permittering.refusjon-beregning", name = "uri")
public class RestRefusjonsberegningClient implements RefusjonsberegningClient {
    private final RefusjonBeregningKonfig konfig;

    @Override
    public RefusjonsberegningResponse beregnRefusjon(RefusjonsberegningRequest request) {
        return WebClient.create()
                .post()
                .uri(konfig.getUri())
                .body(Mono.just(request), RefusjonsberegningRequest.class)
                .retrieve()
                .toEntity(RefusjonsberegningResponse.class)
                .block()
                .getBody();
    }

    @Data
    @ConfigurationProperties(prefix = "permittering.refusjon-beregning")
    private static class RefusjonBeregningKonfig {
        private URI uri;
    }
}
