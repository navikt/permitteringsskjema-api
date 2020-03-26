package no.nav.permitteringsskjemaapi.kodeverk;

import no.nav.permitteringsskjemaapi.SkjemaType;
import no.nav.permitteringsskjemaapi.Årsakskode;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.Map;

@Unprotected
@RestController
@RequestMapping("/kodeverk")
public class KodeverkController {
    @GetMapping
    public Map<String, Map<? extends Enum, String>> alleKoder() {
        return Map.of(
                "årsakskoder", årsakskoder(),
                "skjematyper", skjematyper()
        );
    }

    @GetMapping("/årsakskoder")
    public Map<Årsakskode, String> årsakskoder() {
        var map = new EnumMap<Årsakskode, String>(Årsakskode.class);
        for (Årsakskode årsakskode : Årsakskode.values()) {
            map.put(årsakskode, årsakskode.getNavn());
        }
        return map;
    }

    @GetMapping("/skjematyper")
    public Map<SkjemaType, String> skjematyper() {
        var map = new EnumMap<SkjemaType, String>(SkjemaType.class);
        for (var skjemaType : SkjemaType.values()) {
            map.put(skjemaType, skjemaType.getNavn());
        }
        return map;
    }
}
