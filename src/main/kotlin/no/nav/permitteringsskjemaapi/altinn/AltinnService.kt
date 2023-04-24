package no.nav.permitteringsskjemaapi.altinn

interface AltinnService {
    fun hentOrganisasjoner(): List<AltinnOrganisasjon>
    fun hentOrganisasjonerBasertPåRettigheter(serviceKode: String, serviceEdition: String): List<AltinnOrganisasjon>
}