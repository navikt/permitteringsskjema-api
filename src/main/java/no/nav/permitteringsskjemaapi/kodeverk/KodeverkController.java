package no.nav.permitteringsskjemaapi.kodeverk;

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType;
import no.nav.permitteringsskjemaapi.permittering.Årsakskode;
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
    public Map<PermitteringsskjemaType, String> skjematyper() {
        var map = new EnumMap<PermitteringsskjemaType, String>(PermitteringsskjemaType.class);
        for (var skjemaType : PermitteringsskjemaType.values()) {
            map.put(skjemaType, skjemaType.getNavn());
        }
        return map;
    }
}
