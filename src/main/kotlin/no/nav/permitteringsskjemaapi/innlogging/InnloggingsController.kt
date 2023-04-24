package no.nav.permitteringsskjemaapi.innlogging

import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class InnloggingsController {
    @GetMapping("/innlogget")
    fun erInnlogget() = "ok"
}