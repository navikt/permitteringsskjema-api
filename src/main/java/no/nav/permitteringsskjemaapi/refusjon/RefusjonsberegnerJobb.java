package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
@ConditionalOnLocal
public class RefusjonsberegnerJobb {
    private final RefusjonsberegningRepository repository;
    private final InntektskomponentClient inntektskomponentClient;

    @SneakyThrows
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void innhentOgBeregnUbehandlede() {
        var ubehandlede = repository.findAllByInnhentetTidspunktIsNull();
        if (!ubehandlede.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            log.info("Starter refusjonsberegning, antall={}", ubehandlede.size());
            executor.invokeAll(ubehandlede.stream().map(this::innhentOgBeregn).collect(Collectors.toList()), 10, TimeUnit.SECONDS);
            log.info("Ferdig med refusjonsberegning, antall={}", ubehandlede.size());
        }
    }

    private Callable<Refusjonsberegning> innhentOgBeregn(Refusjonsberegning refusjonsberegning) {
        return () -> {
            BigDecimal innhentetBeløp = inntektskomponentClient.hentInntekt();
            refusjonsberegning.endreInnhentetBeløp(innhentetBeløp);
            return repository.save(refusjonsberegning);
        };
    }
}
