package no.nav.permitteringsskjemaapi.kodeverk

import io.swagger.v3.oas.annotations.Hidden
import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType
import no.nav.permitteringsskjemaapi.permittering.Årsakskode
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*
import java.util.*

@Unprotected
@RestController
@RequestMapping("/kodeverk")
class KodeverkController {
    @Hidden
    @GetMapping
    fun alleKoder() = mapOf(
        "årsakskoder" to årsakskoder(),
        "skjematyper" to skjematyper()
    )

    @GetMapping("/årsakskoder")
    fun årsakskoder(): Map<Årsakskode?, String?> {
        val map = EnumMap<Årsakskode, String?>(Årsakskode::class.java)
        for (årsakskode in Årsakskode.values()) {
            map[årsakskode] = årsakskode.navn
        }
        return map
    }

    @GetMapping("/skjematyper")
    fun skjematyper(): Map<PermitteringsskjemaType?, String?> {
        val map = EnumMap<PermitteringsskjemaType, String>(
            PermitteringsskjemaType::class.java
        )
        for (skjemaType in PermitteringsskjemaType.values()) {
            map[skjemaType] = skjemaType.navn
        }
        return map
    }
}