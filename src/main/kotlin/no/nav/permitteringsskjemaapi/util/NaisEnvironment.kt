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