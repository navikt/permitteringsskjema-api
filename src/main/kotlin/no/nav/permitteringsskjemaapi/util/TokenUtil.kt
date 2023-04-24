package no.nav.permitteringsskjemaapi.util

import no.nav.permitteringsskjemaapi.config.TOKENX_ISSUER
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenUtil(private val ctxHolder: TokenValidationContextHolder) {
    val expiryDate: Date?
        get() = claimSet(TOKENX_ISSUER)?.expirationTime

    val fnrFraToken: String?
        get() = claimSet(TOKENX_ISSUER)?.let { it["pid"] as String }

    fun autentisertBruker(): String {
        return fnrFraToken ?: throw JwtTokenValidatorException("Fant ikke fødselsnummer i token")
    }

    private fun claimSet(issuer: String?): JwtTokenClaims? =
        ctxHolder.tokenValidationContext?.getClaims(issuer)

}