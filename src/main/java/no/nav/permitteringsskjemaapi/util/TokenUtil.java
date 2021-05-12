package no.nav.permitteringsskjemaapi.util;

import static no.nav.permitteringsskjemaapi.config.Constants.SELVBETJENING_ISSUER;
import static no.nav.permitteringsskjemaapi.config.Constants.TOKENX_ISSUER;


import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import no.nav.permitteringsskjemaapi.domain.Fødselsnummer;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;

@Component
public class TokenUtil {
    private final TokenValidationContextHolder ctxHolder;

    public TokenUtil(TokenValidationContextHolder ctxHolder) {
        this.ctxHolder = ctxHolder;
    }

    public boolean erAutentisert() {
        return getFnrFraToken() != null;
    }

    public Date getExpiryDate() {
        if(erInnloggetMedSelvbetjening()) {
            return Optional.ofNullable(claimSet(SELVBETJENING_ISSUER))
                    .map(JwtTokenClaims::getExpirationTime)
                    .orElse(null);
        } else {
            return Optional.ofNullable(claimSet(TOKENX_ISSUER))
                    .map(JwtTokenClaims::getExpirationTime)
                    .orElse(null);
        }
    }

    public String getFnrFraToken() {
        if(erInnloggetMedSelvbetjening()) {
            return Optional.ofNullable(claimSet(SELVBETJENING_ISSUER))
                    .map(JwtTokenClaims::getSubject)
                    .orElse(null);
        } else {
            return Optional.ofNullable(claimSet(TOKENX_ISSUER))
                    .map(jwtTokenClaims -> (String) jwtTokenClaims.get("pid"))
                    .orElse(null);
        }
    }

    public String autentisertBruker() {
        return Optional.ofNullable(getFnrFraToken())
                .orElseThrow(unauthenticated("Fant ikke subject"));
    }

    public String getTokenForInnloggetBruker() {
        if(erInnloggetMedSelvbetjening()) {
            return ctxHolder.getTokenValidationContext().getJwtToken(SELVBETJENING_ISSUER).getTokenAsString();
        } else {
            return ctxHolder.getTokenValidationContext().getJwtToken(TOKENX_ISSUER).getTokenAsString();
        }
    }

    public Fødselsnummer autentisertFNR() {
        return Fødselsnummer.valueOf(autentisertBruker());
    }

    private static Supplier<? extends JwtTokenValidatorException> unauthenticated(String msg) {
        return () -> new JwtTokenValidatorException(msg);
    }

    private JwtTokenClaims claimSet(String issuer) {
        return Optional.ofNullable(context())
                .map(s -> s.getClaims(issuer))
                .orElse(null);
    }

    private boolean erInnloggetMedSelvbetjening() {
        return claimSet(SELVBETJENING_ISSUER) != null;
    }

    private boolean erInnloggetMedTokenX() {
        return claimSet(TOKENX_ISSUER) != null;
    }

    private TokenValidationContext context() {
        return Optional.ofNullable(ctxHolder.getTokenValidationContext())
                .orElse(null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ctxHolder=" + ctxHolder + "]";
    }

}
