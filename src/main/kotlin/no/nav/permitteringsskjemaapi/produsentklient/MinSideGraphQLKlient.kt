package no.nav.permitteringsmelding.notifikasjon.minsideklient.graphql

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.ISO8601DateTime
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.OpprettNySak
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.AltinnMottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.MottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.*
import no.nav.permitteringsskjemaapi.util.NaisEnvironment
import no.nav.permitteringsskjemaapi.util.basedOnEnv
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

private val urlTilNotifikasjonIMiljo = basedOnEnv(
    prod = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    dev = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    other = { "http://localhost:8080" },
)

@Service
class MinSideGraphQLKlient(
    endpoint: String = urlTilNotifikasjonIMiljo,
    private val entraIdClient: EntraIdKlient
) {
    private val log = logger()
    private val client = GraphQLWebClient(url = endpoint)
    private val mottaker = MottakerInput(altinn = AltinnMottakerInput(serviceCode = "5810", serviceEdition = "1"), altinnRessurs = null, naermesteLeder = null)
    // TODO: endre til Altinn3 etter migrering
    // private val mottaker = MottakerInput(altinn = null, altinnRessurs = AltinnRessursMottakerInput(ressursId = "nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger"), naermesteLeder = null)

    private suspend fun hentEntraIdToken(): String {
        val scope = "api://${NaisEnvironment.clusterName}.fager.notifikasjon-produsent-api/.default"
        return entraIdClient.hentToken(scope)
    }

    suspend fun opprettNySak(
        grupperingsid: String,
        merkelapp: String,
        virksomhetsnummer: String,
        tittel: String,
        lenke: String,
        tidspunkt: ISO8601DateTime? = null
    ) {
        val scopedAccessToken = hentEntraIdToken()
        val resultat = client.execute(
            OpprettNySak(
                variables = OpprettNySak.Variables(
                    grupperingsid,
                    merkelapp,
                    virksomhetsnummer,
                    tittel,
                    lenke,
                    tidspunkt,
                    mottaker
                )
            )
        ) {
            header(HttpHeaders.AUTHORIZATION, "Bearer $scopedAccessToken")
        }
        val nySak = resultat.data?.nySak

        if (nySak is NySakVellykket) {
            log.info("Opprettet ny sak {}", nySak.id)
        } else {
            when (nySak) {
                is DuplikatGrupperingsid -> log.info("Sak finnes allerede. hopper over. {}", nySak.feilmelding)
                is UgyldigMerkelapp -> throw Exception(nySak.feilmelding)
                is UgyldigMottaker -> throw Exception(nySak.feilmelding)
                is UkjentProdusent -> throw Exception(nySak.feilmelding)
                is UkjentRolle -> throw Exception(nySak.feilmelding)
                else -> {
                    throw Exception(resultat.errors?.joinToString { it.message })
                }
            }
        }
    }
}
