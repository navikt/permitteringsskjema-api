package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static no.nav.permitteringsskjemaapi.util.StreamUtil.not;

@Component
@AllArgsConstructor
@Slf4j
@ConditionalOnLocal
public class RefusjonsberegnerJobb implements DisposableBean {
    private final ArbeidsforholdRepository repository;
    private final RefusjonsberegningClient refusjonsberegningClient;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @SneakyThrows
    @Scheduled(fixedDelay = 1000, initialDelay = 10000)
    public void innhentOgBeregnUbehandlede() {
        List<Arbeidsforhold> ubehandlede = repository.findAllByInnhentetTidspunktIsNullOrderByFnr();
        int antallUbehandlede = ubehandlede.size();
        if (antallUbehandlede > 0 && executor.getActiveCount() == 0) {
            log.info("Starter refusjonsberegning, antall={}", ubehandlede.size());
            var futures = executor.invokeAll(ubehandlede.stream().map(this::innhentOgBeregn).collect(Collectors.toList()), 15, TimeUnit.SECONDS);
            long antallFullførte = futures.stream().filter(not(Future::isCancelled)).count();
            if (antallFullførte == antallUbehandlede) {
                log.info("Refusjonsberegning fullført, antall={}", antallFullførte);
            } else {
                log.info("Refusjonsberegning delvis fullført, antall={} av totalt={}", antallFullførte, antallUbehandlede);
            }
        }
    }

    private Callable<Arbeidsforhold> innhentOgBeregn(Arbeidsforhold arbeidsforhold) {
        return () -> {
            RefusjonsberegningRequest request = new RefusjonsberegningRequest(arbeidsforhold.getFnr(), arbeidsforhold.getBedriftNr(), arbeidsforhold.getGradering(), arbeidsforhold.getPeriodeStart(), arbeidsforhold.getPeriodeSlutt());
            RefusjonsberegningResponse response = refusjonsberegningClient.beregnRefusjon(request);
            arbeidsforhold.settInnhentetInformasjon(response);

            // Sjekken behøves fordi raden kan være slettet i tiden fra jobben startet til nå
            if (repository.existsById(arbeidsforhold.getId())) {
                return repository.save(arbeidsforhold);
            }

            return null;
        };
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
