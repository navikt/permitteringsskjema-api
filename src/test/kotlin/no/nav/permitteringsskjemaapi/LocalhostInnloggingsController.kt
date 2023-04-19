package no.nav.permitteringsskjemaapi;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class LocalhostInnloggingsController {

    private MockOAuth2Server mockOAuth2Server;

    @Autowired
    public LocalhostInnloggingsController(MockOAuth2Server mockOAuth2Server) {
        this.mockOAuth2Server = mockOAuth2Server;
    }

    @Unprotected
    @RequestMapping(value="/auth/mock-token", method = RequestMethod.GET)
    public String getMockToken(@RequestParam(value = "redirect") String redirect, HttpServletResponse response) throws IOException {
        SignedJWT signedJWT = this.mockOAuth2Server.issueToken("tokenx", "19097302327", "tokenx", Map.of("pid", "19097302327"));
        Cookie cookie = new Cookie("localhost-idtoken", signedJWT.serialize());
        cookie.setDomain("localhost");
        cookie.setPath("/");
        response.addCookie(cookie);
        response.sendRedirect(redirect);
        return null;
    }
}
