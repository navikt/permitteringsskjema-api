package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/arbeidsforhold")
@Protected
public class ArbeidsforholdController {
    private final TokenUtil fnrExtractor;
    private final ArbeidsforholdRepository arbeidsforholdRepository;
    private final RefusjonsskjemaRepository refusjonsskjemaRepository;

    @PostMapping
    public List<Arbeidsforhold> opprett(@RequestBody LeggTilArbeidsforhold request) {
        Refusjonsskjema refusjonsskjema = refusjonsskjemaRepository.findByIdAndOpprettetAv(request.getRefusjonsskjemaId(), fnrExtractor.autentisertBruker())
                .orElseThrow(IkkeFunnetException::new);
        List<Arbeidsforhold> nye = Arbeidsforhold.opprett(refusjonsskjema, request.getFnr(), request.getGradering(), request.getPeriodeStart(), request.getPeriodeSlutt());
        return arbeidsforholdRepository.saveAll(nye);
    }

    @PostMapping("/slett")
    public void slett(@RequestBody List<String> fnr) {
        arbeidsforholdRepository.deleteAll(arbeidsforholdRepository.findAllByFnrIn(fnr));
    }

    @GetMapping
    public Page<Arbeidsforhold> hent(BeregningQueryParametre queryParametre, Pageable pageable) {
        return arbeidsforholdRepository.findAllByRefusjonsskjemaIdAndOpprettetAv(
                queryParametre.getRefusjonsskjemaId(),
                fnrExtractor.autentisertBruker(), pageable);
    }

}
