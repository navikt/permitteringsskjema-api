package no.nav.permitteringsskjemaapi.util;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class FnrExtractor {
    private static final String ISSUER = "selvbetjening";
    private TokenValidationContextHolder context;

    public FnrExtractor(TokenValidationContextHolder context) {
        this.context = context;
    }

    public String extract() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            throw new JwtTokenUnauthorizedException("FnrExtractor: Token not found.");
        }

        return getFnr(ISSUER);
    }

    private String getFnr(String issuer) {
        return getTokenContext().getJwtToken(issuer).getSubject();
    }

    private TokenValidationContext getTokenContext() {
        return context.getTokenValidationContext();
    }

}
