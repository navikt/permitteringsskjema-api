package no.nav.permitteringsskjemaapi.altinn

import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.*

@RestController
@Protected
class OrganisasjonerController(
    private val altinnService: AltinnService
) {

    @GetMapping("/organisasjoner-v2")
    fun hentOrganisasjonerV2() = altinnService.hentAltinnTilganger(INNSYN_ALLE_PERMITTERINGSSKJEMA).hierarki
}