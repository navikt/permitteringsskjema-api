package no.nav.permitteringsskjemaapi.notifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.ISO8601DateTime
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.OpprettNySak
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.AltinnRessursMottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.MottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.*
import no.nav.permitteringsskjemaapi.util.NaisEnvironment
import no.nav.permitteringsskjemaapi.util.urlTilNotifikasjonIMiljo
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class ProdusentApiKlient(
    private val entraIdClient: EntraIdKlient
) {
    private val log = logger()
    private val client = GraphQLWebClient(url = urlTilNotifikasjonIMiljo)

    private val mottaker = MottakerInput(
        altinn = null,
        altinnRessurs = AltinnRessursMottakerInput(ressursId = INNSYN_ALLE_PERMITTERINGSSKJEMA),
        naermesteLeder = null
    )

    private suspend fun hentEntraIdToken(): String {
        val scope = "api://${NaisEnvironment.clusterName}.fager.notifikasjon-produsent-api/.default"
        return entraIdClient.getToken(scope)
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
        val resultat = runBlocking {
            client.execute(
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