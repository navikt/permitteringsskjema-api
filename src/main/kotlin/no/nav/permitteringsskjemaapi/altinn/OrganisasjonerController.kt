package no.nav.permitteringsskjemaapi.altinn

import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.*

@RestController
@Protected
class OrganisasjonerController(
    private val altinnService: AltinnService
) {

    @GetMapping("/organisasjoner")
    fun hentOrganisasjoner() = altinnService.hentOrganisasjoner()

    @GetMapping("/organisasjoner-v2")
    fun hentOrganisasjonerV2() = altinnService.hentAltinnTilganger().hierarki
}