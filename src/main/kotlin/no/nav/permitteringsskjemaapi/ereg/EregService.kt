package no.nav.permitteringsskjemaapi.ereg

import com.fasterxml.jackson.databind.JsonNode
import no.nav.permitteringsskjemaapi.altinn.AltinnOrganisasjon
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
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

    fun hentUnderenhet(virksomhetsnummer: String?): AltinnOrganisasjon? {
        return try {
            val json = restTemplate.getForEntity(
                "/v1/organisasjon/{virksomhetsnummer}?inkluderHierarki=true",
                JsonNode::class.java,
                mapOf("virksomhetsnummer" to virksomhetsnummer)
            ).body
            underenhet(json)
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                return null
            }
            throw e
        }
    }

    fun hentOverenhet(orgnummer: String?): AltinnOrganisasjon? {
        return try {
            val json = restTemplate.getForEntity(
                "/v1/organisasjon/{orgnummer}",
                JsonNode::class.java,
                mapOf("orgnummer" to orgnummer)
            ).body
            overenhet(json)
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                return null
            }
            throw e
        }
    }

    fun underenhet(json: JsonNode?): AltinnOrganisasjon? {
        if (json == null || json.isEmpty) {
            return null
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

    fun overenhet(json: JsonNode?): AltinnOrganisasjon? {
        return if (json == null) {
            null
        } else AltinnOrganisasjon(
            samletNavn(json),
            "Enterprise",
            null,
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