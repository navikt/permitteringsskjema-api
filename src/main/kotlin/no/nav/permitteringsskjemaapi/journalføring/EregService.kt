package no.nav.permitteringsskjemaapi.journalføring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.permitteringsskjemaapi.util.retryInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import java.time.LocalDate
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

    fun hentKommunenummer(virksomhetsnummer: String): String? {
        val eregEnhet = restTemplate.getForEntity(
            "/v1/organisasjon/{virksomhetsnummer}?inkluderHierarki=true",
            EregEnhet::class.java,
            mapOf("virksomhetsnummer" to virksomhetsnummer)
        ).body ?: throw RuntimeException("null response for underenhet $virksomhetsnummer")

        return eregEnhet.kommuneNummer
    }

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
