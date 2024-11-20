package no.nav.permitteringsmelding.notifikasjon.minsideklient.graphql

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import no.nav.permitteringsmelding.notifikasjon.autentisering.Oauth2Client
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.*
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.ISO8601DateTime
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.OpprettNySak
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.AltinnMottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.MottakerInput
import no.nav.permitteringsskjemaapi.tokenx.TokenExchangeClient
import no.nav.permitteringsskjemaapi.util.basedOnEnv
import org.springframework.http.HttpHeaders
import java.net.http.HttpClient


private val urlTilNotifikasjonIMiljo = basedOnEnv(
    prod = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    dev = { "http://notifikasjon-produsent-api.fager/api/graphql" },
    other = { "http://localhost:8080" },
)

private val notifikasjonerScope = basedOnEnv(
    prod = { "api://prod-gcp.fager.notifikasjon-produsent-api/.default" },
    dev = { "api://dev-gcp.fager.notifikasjon-produsent-api/.default" },
    other = { "lokal" },
)
//
//private val defaultHttpClient = HttpClient() {
//    install(ContentNegotiation) {
//        jackson {
//            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            setSerializationInclusion(JsonInclude.Include.NON_NULL)
//        }
//    }
//}

class MinSideGraphQLKlient(
    endpoint: String = urlTilNotifikasjonIMiljo,
    //TODO: trengs denne?httpClient: HttpClient = defaultHttpClient,
    private val tokenExchangeClient: TokenExchangeClient,
    private val oauth2Client: Oauth2Client //TODO: rimelig sikker på at vi trenger denne for å snakke med produsent apiet
) {

    private val log = logger()
    private val client = GraphQLWebClient(url = endpoint)
    private val mottaker = MottakerInput(altinn = AltinnMottakerInput(serviceCode = "5810", serviceEdition = "1"), altinnRessurs = null, naermesteLeder = null)
    // TODO: endre til Altinn3 etter migrering
    // private val mottaker = MottakerInput(altinn = null, altinnRessurs = AltinnRessursMottakerInput(ressursId = "nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger"), naermesteLeder = null)

    suspend fun opprettNySak(
        grupperingsid: String,
        merkelapp: String,
        virksomhetsnummer: String,
        tittel: String,
        lenke: String,
        tidspunkt: ISO8601DateTime? = null
    ) {
        val scopedAccessToken = tokenExchangeClient.exchange(authenticatedUserHolder.token,)

        val scopedAccessToken = oauth2Client.machine2machine().accessToken
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
