package no.nav.permitteringsskjemaapi.altinn

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class FakeAltinnService : AltinnService {
    override fun hentOrganisasjoner() = listOf(
        AltinnOrganisasjon(
            "STORFOSNA OG FREDRIKSTAD REGNSKAP",
            "Business",
            "910825569",
            "BEDR",
            "Active",
            "910825550"
        ),
        AltinnOrganisasjon("BALLSTAD OG HORTEN", "Enterprise", "910825550", "AS", "Active", null),
        AltinnOrganisasjon("BAREKSTAD OG YTTERVÅG REGNSKAP", "Enterprise", "910998250", "AS", "Active", null),
        AltinnOrganisasjon("BALLSTAD OG HORTEN", "Business", "810514442", "BEDR", "Active", "910998250")
    )

    override fun hentOrganisasjonerBasertPåRettigheter(
        serviceKode: String,
        serviceEdition: String
    ) = listOf(
        AltinnOrganisasjon("SALTRØD OG HØNSEBY", "Business", "910825569", "BEDR", "Active", "910825550"),
        AltinnOrganisasjon("BALLSTAD OG HORTEN", "Enterprise", "910825550", "AS", "Active", null),
        AltinnOrganisasjon("BAREKSTAD OG YTTERVÅG REGNSKAP", "Enterprise", "910998250", "AS", "Active", null),
        AltinnOrganisasjon("BALLSTAD OG HORTEN", "Business", "810514442", "BEDR", "Active", "910998250")
    )
}