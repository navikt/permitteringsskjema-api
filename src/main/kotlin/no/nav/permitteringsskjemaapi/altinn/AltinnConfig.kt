package no.nav.permitteringsskjemaapi.altinn

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "altinn")
data class AltinnConfig(
    var altinnProxyUrl: String = ""
)