package no.nav.permitteringsskjemaapi.permittering


data class OpprettPermitteringsskjema(
    var bedriftNr: String? = null,
    var type: PermitteringsskjemaType? = null,
)