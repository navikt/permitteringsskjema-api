package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.config.GittMiljø
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientResponseException
import java.time.LocalDate

fun interface EregClient {
    fun hentKommunenummer(virksomhetsnummer: String): String?
}

@Component
class EregClientImpl(
    @Value("\${ereg-services.baseUrl}") eregBaseUrl: String?,
    private val gittMiljø: GittMiljø,
    restTemplateBuilder: RestTemplateBuilder
): EregClient {
    private val restTemplate = restTemplateBuilder
        .rootUri(eregBaseUrl)
        .additionalInterceptors(
            retryInterceptor(
                3,
                250L,
                java.net.SocketException::class.java,
                javax.net.ssl.SSLHandshakeException::class.java,
                HttpServerErrorException.BadGateway::class.java,
                HttpServerErrorException.GatewayTimeout::class.java,
                HttpServerErrorException.ServiceUnavailable::class.java,
            )
        )
        .build()

    override fun hentKommunenummer(virksomhetsnummer: String): String? {
        val eregEnhet = try {
            restTemplate.getForEntity(
                "/v1/organisasjon/{virksomhetsnummer}?inkluderHierarki=true",
                EregEnhet::class.java,
                mapOf("virksomhetsnummer" to virksomhetsnummer)
            ).body ?: throw RuntimeException("body fra ereg er null")
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                return gittMiljø.resolve(
                    dev = { null },
                    other = { throw VirksomhetNotFoundException(e) },
                )
            }
            throw e
        }

        return eregEnhet.kommuneNummer
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class EregEnhet(
        val organisasjonDetaljer: OrganisasjonDetaljer
    ) {
        private val adresser = organisasjonDetaljer.forretningsadresser + organisasjonDetaljer.postadresser

        val kommuneNummer: String?
            get() = adresser
                .filter { it.gyldighetsperiode.tom == null }
                .firstNotNullOfOrNull { it.kommunenummer }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OrganisasjonDetaljer(
        val forretningsadresser: List<Adresse> = listOf(),
        val postadresser: List<Adresse> = listOf(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Adresse(
        val kommunenummer: String? = null,
        val gyldighetsperiode: Gyldighetsperiode,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Gyldighetsperiode(
        val tom: LocalDate? = null,
    )
}

class VirksomhetNotFoundException(cause: Exception? = null) : RuntimeException(cause)
