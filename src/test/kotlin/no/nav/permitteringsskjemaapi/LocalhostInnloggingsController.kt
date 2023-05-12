package no.nav.permitteringsskjemaapi

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("mockoauth")
class LocalhostInnloggingsController @Autowired constructor(private val mockOAuth2Server: MockOAuth2Server) {
    @Unprotected
    @RequestMapping(value = ["/auth/mock-token"], method = [RequestMethod.GET])
    fun getMockToken(@RequestParam(value = "redirect") redirect: String?, response: HttpServletResponse): String? {
        val signedJWT = mockOAuth2Server.issueToken("tokenx", "19097302327", "tokenx", mapOf("pid" to "19097302327"))
        val cookie = Cookie("localhost-idtoken", signedJWT.serialize())
        cookie.domain = "localhost"
        cookie.path = "/"
        response.addCookie(cookie)
        response.sendRedirect(redirect)
        return null
    }
}