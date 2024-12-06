package no.nav.permitteringsskjemaapi.entraID

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class AccessTokenHolderTest {

    @Test
    fun `token expires correctly`() {
        val now = Instant.now()
        val token = AccessTokenHolder(
            tokenResponse = TokenResponse(
                access_token = "token",
                token_type = "type",
                expires_in = 3600,
            ),
            createdAt = now
        )
        assertFalse(token.hasExpired(now))
        assertTrue(token.hasExpired(now.plusSeconds(3600)))
        assertFalse(token.hasExpired(now.plusSeconds(3600L - token_expiry_buffer_seconds)))
        assertTrue(token.hasExpired(now.plusSeconds(3600L - token_expiry_buffer_seconds + 1)))
    }
}