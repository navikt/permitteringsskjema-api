package no.nav.permitteringsskjemaapi.refusjon;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.refusjon.domenehendelser.RefusjonsskjemaEndret;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class OpprettRefusjonsberegning {
    private final RefusjonsberegningRepository repository;

    @EventListener
    public void opprettRefusjonsberegninger(RefusjonsskjemaEndret event) {
        Map<String, Refusjonsberegning> fnrBeregningMap = repository.findAllByRefusjonsskjemaId(event.getRefusjonsskjema().getId())
                .stream()
                .collect(Collectors.toMap(Refusjonsberegning::getFnr, Function.identity()));
        Map<String, Arbeidsforhold> fnrSkjemaMap = event.getRefusjonsskjema().getArbeidsforhold()
                .stream()
                .collect(Collectors.toMap(Arbeidsforhold::getFnr, Function.identity()));

        var nyeFnr = Sets.difference(fnrSkjemaMap.keySet(), fnrBeregningMap.keySet());
        var slettedeFnr = Sets.difference(fnrBeregningMap.keySet(), fnrSkjemaMap.keySet());

        var beregningerSomErNye = new ArrayList<Refusjonsberegning>();
        nyeFnr.forEach(fnr -> {
            beregningerSomErNye.add(Refusjonsberegning.opprett(fnrSkjemaMap.get(fnr)));
        });

        var beregningerSomSkalSlettes = new ArrayList<Refusjonsberegning>();
        slettedeFnr.forEach(fnr -> {
            beregningerSomSkalSlettes.add(fnrBeregningMap.get(fnr));
        });

        repository.saveAll(beregningerSomErNye);
        repository.deleteAll(beregningerSomSkalSlettes);
    }
}
