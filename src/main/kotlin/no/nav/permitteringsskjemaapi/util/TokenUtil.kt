package no.nav.permitteringsskjemaapi.util

import no.nav.permitteringsskjemaapi.config.TOKENX_ISSUER
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenUtil(private val ctxHolder: TokenValidationContextHolder) {
    private val claimSet: JwtTokenClaims
        get() = ctxHolder.getTokenValidationContext().getClaims(TOKENX_ISSUER)

    val expiryDate: Date?
        get() = claimSet.expirationTime

    val fnrFraToken: String?
        get() = claimSet.getStringClaim("pid")

    fun autentisertBruker(): String {
        return fnrFraToken ?: throw JwtTokenValidatorException("Fant ikke fødselsnummer i token")
    }
}