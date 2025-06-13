package no.nav.permitteringsskjemaapi.notifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.permitteringsskjemaapi.config.INNSYN_ALLE_PERMITTERINGSSKJEMA
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.entraID.EntraIdKlient
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.ISO8601DateTime
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.OpprettNyBeskjed
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.OpprettNySak
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.AltinnRessursMottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.inputs.MottakerInput
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.DefaultNyBeskjedResultatImplementation
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.DuplikatEksternIdOgMerkelapp
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.NyBeskjedVellykket
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.DefaultNySakResultatImplementation
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.DuplikatGrupperingsid
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.DuplikatGrupperingsidEtterDelete
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.NySakVellykket
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.UgyldigMerkelapp as NySakUgyldigMerkelapp
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.UgyldigMottaker as NySakUgyldigMottaker
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.UkjentProdusent as NySakUkjentProdusent
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnysak.UkjentRolle as NySakUkjentRolle
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.UgyldigMerkelapp as NyBeskjedUgyldigMerkelapp
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.UgyldigMottaker as NyBeskjedUgyldigMottaker
import no.nav.permitteringsskjemaapi.notifikasjon.graphql.generated.opprettnybeskjed.UkjentProdusent as NyBeskjedUkjentProdusent
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

    private fun hentEntraIdToken(): String {
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
            header(HttpHeaders.AUTHORIZATION, "Bearer ${hentEntraIdToken()}")
        }

        when (val nySak = resultat.data?.nySak) {
            null -> throw Exception("Uventet feil: Ny sak er null, resultat: $resultat")

            is NySakVellykket -> log.info("Opprettet ny sak {}", nySak.id)

            is DuplikatGrupperingsid -> log.info("Sak finnes allerede. hopper over. {}", nySak.feilmelding)
            is DuplikatGrupperingsidEtterDelete -> log.info("Sak finnes allerede. hopper over. {}", nySak.feilmelding)

            is NySakUgyldigMerkelapp -> throw Exception(nySak.feilmelding)
            is NySakUgyldigMottaker -> throw Exception(nySak.feilmelding)
            is NySakUkjentProdusent -> throw Exception(nySak.feilmelding)
            is NySakUkjentRolle -> throw Exception(nySak.feilmelding)

            is DefaultNySakResultatImplementation -> throw Exception("Uventet feil: $resultat")
        }
    }

    suspend fun opprettNyBeskjed(
        grupperingsid: String,
        merkelapp: String,
        virksomhetsnummer: String,
        tekst: String,
        lenke: String,
        tidspunkt: ISO8601DateTime? = null
    ) {
        val resultat = client.execute(
            OpprettNyBeskjed(
                variables = OpprettNyBeskjed.Variables(
                    grupperingsid,
                    merkelapp,
                    virksomhetsnummer,
                    tekst,
                    lenke,
                    tidspunkt,
                    mottaker
                )
            )
        ) {
            header(HttpHeaders.AUTHORIZATION, "Bearer ${hentEntraIdToken()}")
        }

        when (val nyBeskjed = resultat.data?.nyBeskjed) {
            null -> throw Exception("Uventet feil: Ny beskjed er null, resultat: $resultat")

            is NyBeskjedVellykket -> log.info("Opprettet ny beskjed {}", nyBeskjed.id)

            is DuplikatEksternIdOgMerkelapp -> log.info(
                "Beskjed finnes allerede. hopper over. {}",
                nyBeskjed.feilmelding
            )

            is NyBeskjedUgyldigMerkelapp -> throw Exception(nyBeskjed.feilmelding)
            is NyBeskjedUgyldigMottaker -> throw Exception(nyBeskjed.feilmelding)
            is NyBeskjedUkjentProdusent -> throw Exception(nyBeskjed.feilmelding)

            is DefaultNyBeskjedResultatImplementation -> throw Exception("Uventet feil: $resultat")
        }
    }
}