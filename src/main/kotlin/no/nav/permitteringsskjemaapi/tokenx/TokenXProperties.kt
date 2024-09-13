package no.nav.permitteringsskjemaapi.tokenx

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import no.nav.permitteringsskjemaapi.config.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.text.ParseException

@Profile("local", "dev-gcp", "prod-gcp")
@Configuration
@ConfigurationProperties("token.x")
class TokenXProperties(
    var clientId: String = "",
    var issuer: String = "",
    var privateJwk: String = "",
    var tokenEndpoint: String = "",
) : InitializingBean {
    val log = logger()

    val privateJwkRsa by lazy { parsePrivateJwk() }
    val jwsSigner by lazy { createJWSSigner() }

    fun parsePrivateJwk(): RSAKey {
        return try {
            RSAKey.parse(privateJwk)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    fun createJWSSigner(): JWSSigner {
        return try {
            RSASSASigner(privateJwkRsa)
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
    }

    override fun afterPropertiesSet() {
        log.info(
            "TokenX configured with issuer={} clientId={} tokenEndpoint={}",
            issuer,
            clientId,
            tokenEndpoint
        )
    }

    companion object {
        const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange"
        const val CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
        const val SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"
    }
}