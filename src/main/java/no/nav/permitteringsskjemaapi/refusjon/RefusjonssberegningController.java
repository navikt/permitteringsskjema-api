package no.nav.permitteringsskjemaapi.refusjon;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/refusjonsberegning")
@Protected
public class RefusjonssberegningController {
    private final TokenUtil fnrExtractor;
    private final RefusjonsberegningRepository repository;

    @GetMapping
    public List<Refusjonsberegning> hent(BeregningQueryParametre queryParametre) {

        return repository.findAllByRefusjonsskjemaIdAndOpprettetAv(
                queryParametre.getRefusjonsskjemaId(),
                fnrExtractor.autentisertBruker());
    }
}
