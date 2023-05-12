package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.databind.JsonNode
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component
class EregService(
    @Value("\${ereg-services.baseUrl}") eregBaseUrl: String?,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val restTemplate = restTemplateBuilder
        .rootUri(eregBaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                java.net.SocketException::class.java,
                javax.net.ssl.SSLHandshakeException::class.java,
            )
        )
        .build()

    fun hentUnderenhet(virksomhetsnummer: String?): AltinnOrganisasjon {
        val json = restTemplate.getForEntity(
            "/v1/organisasjon/{virksomhetsnummer}?inkluderHierarki=true",
            JsonNode::class.java,
            mapOf("virksomhetsnummer" to virksomhetsnummer)
        ).body

        if (json == null || json.isEmpty) {
            throw RuntimeException("null/empty json response for underenhet $virksomhetsnummer")
        }
        val juridiskOrgnummer = json.at("/inngaarIJuridiskEnheter/0/organisasjonsnummer").asText()
        val orgleddOrgnummer = json.at("/bestaarAvOrganisasjonsledd/0/organisasjonsledd/organisasjonsnummer").asText()
        val orgnummerTilOverenhet = orgleddOrgnummer.ifBlank { juridiskOrgnummer }
        return AltinnOrganisasjon(
            samletNavn(json),
            "Business",
            orgnummerTilOverenhet,
            json.at("/organisasjonsnummer").asText(),
            json.at("/organisasjonDetaljer/enhetstyper/0/enhetstype").asText(),
            "Active"
        )
    }

    companion object {
        private fun samletNavn(json: JsonNode) = listOf(
            json.at("/navn/navnelinje1").asText(null),
            json.at("/navn/navnelinje2").asText(null),
            json.at("/navn/navnelinje3").asText(null),
            json.at("/navn/navnelinje4").asText(null),
            json.at("/navn/navnelinje5").asText(null)
        )
            .filter(Objects::nonNull)
            .joinToString(" ")
    }
}