package no.nav.permitteringsskjemaapi.innlogging;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.token.support.core.api.Unprotected;
import org.hibernate.service.spi.InjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@Profile("local")
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
