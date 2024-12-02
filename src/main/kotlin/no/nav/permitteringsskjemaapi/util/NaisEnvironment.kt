package no.nav.permitteringsskjemaapi.util

object NaisEnvironment {
    val clusterName: String = System.getenv("NAIS_CLUSTER_NAME") ?: ""
}

fun <T> basedOnEnv(
    other: () -> T,
    prod: () -> T = other,
    dev: () -> T = other,
): T =
    when (NaisEnvironment.clusterName) {
        "prod-gcp" -> prod()
        "dev-gcp" -> dev()
        else -> other()
    }

val urlTilNotifikasjonIMiljo = basedOnEnv(
    prod = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    dev = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    other = { "http://localhost:54058/permitteringsskjema-api/graphql" }, // brukes i tester
)

val urlTilPermitteringsløsningFrontend = basedOnEnv(
    prod = { "https://arbeidsgiver.nav.no/permittering/skjema/kvitteringsside/" },
    dev = { "https://permitteringsskjema.intern.dev.nav.no/permittering/skjema/kvitteringsside/" },
    other = { "http://localhost:8080" },
)