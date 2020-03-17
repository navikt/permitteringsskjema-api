package no.nav.permitteringsskjemaapi;

import lombok.AllArgsConstructor;
import no.nav.permitteringsskjemaapi.tjenester.permittering.Permittering;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/skjema")
@Unprotected
public class PermitteringsskjemaController {
    private final PermitteringsskjemaRepository repository;

    @GetMapping("/{orgNr}/{id}")
    public Permitteringsskjema hent(@PathVariable String orgNr, @PathVariable UUID id) {
        return repository.findByIdAndOrgNr(id, orgNr);
    }

    @GetMapping("/{orgNr}")
    public List<Permitteringsskjema> hent(@PathVariable String orgNr) {
        return repository.findAllByOrgNr(orgNr);
    }

    @PostMapping("/{orgNr}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Permitteringsskjema opprett(@PathVariable String orgNr) {
        Permitteringsskjema skjema = Permitteringsskjema.nyttSkjema(orgNr);
        return repository.save(skjema);
    }

    @PutMapping("/{orgNr}/{id}")
    public Permitteringsskjema endre(@PathVariable String orgNr, @PathVariable UUID id, @RequestBody EndreSkjema endreSkjema) {
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOrgNr(id, orgNr);
        permitteringsskjema.endre(endreSkjema);
        return repository.save(permitteringsskjema);
    }

    @PostMapping("/{orgNr}/{id}/send-inn")
    public Permitteringsskjema sendInn(@PathVariable String orgNr, @PathVariable UUID id) {
        Permitteringsskjema permitteringsskjema = repository.findByIdAndOrgNr(id, orgNr);
        permitteringsskjema.sendInn();
        return repository.save(permitteringsskjema);
    }
}
