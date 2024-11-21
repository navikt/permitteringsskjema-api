package no.nav.permitteringsskjemaapi.entraID

import org.springframework.stereotype.Service

interface EntraIdConfig {
    val TOKEN_ENDPOINT_URL: String
    val CLIENT_ID: String
    val CLIENT_SECRET: String
}

@Service
class EntraIdConfigImpl: EntraIdConfig {
    override val TOKEN_ENDPOINT_URL = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT") ?: "http://localhost:8080"
    override val CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID")
    override val CLIENT_SECRET = System.getenv("AZURE_APP_CLIENT_SECRET")
}