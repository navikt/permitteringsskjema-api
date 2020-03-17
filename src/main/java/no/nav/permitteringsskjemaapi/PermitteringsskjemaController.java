package no.nav.permitteringsskjemaapi;

import lombok.AllArgsConstructor;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/skjema")
@Unprotected
public class PermitteringsskjemaController {
    private final PermitteringsskjemaRepository repository;

    @GetMapping("/{id}")
    public Optional<Permitteringsskjema> hent(@PathVariable UUID id) {
        return repository.findById(id);
    }

    @GetMapping
    public List<Permitteringsskjema> hent() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Permitteringsskjema opprett(@RequestBody OpprettSkjema opprettSkjema) {
        Permitteringsskjema skjema = Permitteringsskjema.opprettSkjema(opprettSkjema);
        return repository.save(skjema);
    }

    @PutMapping("/{id}")
    public Permitteringsskjema endre(@PathVariable UUID id, @RequestBody EndreSkjema endreSkjema) {
        Permitteringsskjema permitteringsskjema = repository.findById(id).orElseThrow();
        permitteringsskjema.endre(endreSkjema);
        return repository.save(permitteringsskjema);
    }

    @PostMapping("/{id}/send-inn")
    public Permitteringsskjema sendInn(@PathVariable UUID id) {
        Permitteringsskjema permitteringsskjema = repository.findById(id).orElseThrow();
        permitteringsskjema.sendInn();
        return repository.save(permitteringsskjema);
    }
}
