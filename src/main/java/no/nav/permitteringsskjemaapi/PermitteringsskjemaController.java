package no.nav.permitteringsskjemaapi;

import lombok.AllArgsConstructor;
import no.nav.security.token.support.core.api.Unprotected;
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

    @PostMapping("/{orgNr}/{id}")
    public void lagre(@PathVariable String orgNr, @PathVariable UUID id, @RequestBody Permitteringsskjema skjema) {
        skjema.setId(id);
        skjema.setOrgNr(orgNr);
        repository.save(skjema);
    }
}
