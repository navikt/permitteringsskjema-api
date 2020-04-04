package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static no.nav.permitteringsskjemaapi.util.StreamUtil.not;

@Component
@AllArgsConstructor
@Slf4j
@ConditionalOnLocal
public class RefusjonsberegnerJobb implements DisposableBean {
    private final RefusjonsberegningRepository repository;
    private final InntektskomponentClient inntektskomponentClient;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @SneakyThrows
    @Scheduled(fixedDelay = 1000, initialDelay = 10000)
    public void innhentOgBeregnUbehandlede() {
        List<Refusjonsberegning> ubehandlede = repository.findAllByInnhentetTidspunktIsNullOrderByFnr();
        int antallUbehandlede = ubehandlede.size();
        if (antallUbehandlede > 0 && executor.getActiveCount() == 0) {
            log.info("Starter refusjonsberegning, antall={}", ubehandlede.size());
            var futures = executor.invokeAll(ubehandlede.stream().map(this::innhentOgBeregn).collect(Collectors.toList()), 30, TimeUnit.SECONDS);
            long antallFullførte = futures.stream().filter(not(Future::isCancelled)).count();
            if (antallFullførte == antallUbehandlede) {
                log.info("Refusjonsberegning fullført, antall={}", antallFullførte);
            } else {
                log.info("Refusjonsberegning delvis fullført, antall={} av totalt={}", antallFullførte, antallUbehandlede);
            }
        }
    }

    private Callable<Refusjonsberegning> innhentOgBeregn(Refusjonsberegning refusjonsberegning) {
        return () -> {
            BigDecimal innhentetBeløp = inntektskomponentClient.hentInntekt();
            refusjonsberegning.endreInnhentetBeløp(innhentetBeløp);
            return repository.save(refusjonsberegning);
        };
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
