package no.nav.permitteringsskjemaapi.fakes

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@TestConfiguration
class FakeControllerContext {

    @RestController
    @Unprotected // Tester ikke autorisasjon her
    @Profile("test")
    class FakeController(private val config: FakeResponseResolver) {

        @PostMapping("/*")
        fun getResponse(): Any {
            return config.resolveResponse()
        }
    }

    @Component
    class FakeResponseResolver {
        private var resolver: () -> Any = { throw Exception("No resolver set") }

        fun setResolver(resolver: () -> Any) {
            this.resolver = resolver
        }

        fun resolveResponse(): Any {
            return this.resolver.invoke()
        }
    }
}