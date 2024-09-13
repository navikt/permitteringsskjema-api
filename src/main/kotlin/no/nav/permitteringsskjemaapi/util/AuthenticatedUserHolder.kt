package no.nav.permitteringsskjemaapi.util

import no.nav.permitteringsskjemaapi.config.TOKENX_ISSUER
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthenticatedUserHolder(private val ctxHolder: TokenValidationContextHolder) {
    val token: String
        get() = jwtToken.encodedToken

    private val jwtToken: JwtToken
        get() = ctxHolder.getTokenValidationContext()
            .firstValidToken ?: throw NoSuchElementException("no valid token. how did you get so far without a valid token?")

    private val claimSet: JwtTokenClaims
        get() = ctxHolder.getTokenValidationContext().getClaims(TOKENX_ISSUER)

    val expiryDate: Date?
        get() = claimSet.expirationTime

    val fnr: String?
        get() = jwtToken.jwtTokenClaims.getStringClaim("pid")

    fun autentisertBruker(): String {
        return fnr ?: throw JwtTokenValidatorException("Fant ikke fødselsnummer i token")
    }
}