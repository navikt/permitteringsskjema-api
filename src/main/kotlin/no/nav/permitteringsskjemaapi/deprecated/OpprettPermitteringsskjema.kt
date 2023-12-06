package no.nav.permitteringsskjemaapi.deprecated

import no.nav.permitteringsskjemaapi.permittering.PermitteringsskjemaType


data class OpprettPermitteringsskjema(
    var bedriftNr: String? = null,
    var type: PermitteringsskjemaType? = null,
)