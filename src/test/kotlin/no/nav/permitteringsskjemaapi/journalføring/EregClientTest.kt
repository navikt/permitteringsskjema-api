package no.nav.permitteringsskjemaapi.journalføring

import no.nav.permitteringsskjemaapi.config.GittMiljø
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException

@MockBean(MultiIssuerConfiguration::class)
@RestClientTest(
    components = [EregClient::class, GittMiljø::class],
    properties = [
        "spring.profiles.active=prod-gcp", // later som vi er i prod for å teste funksjonalitet som bare er i prod
    ]
)
class EregClientTest {

    @Autowired
    lateinit var eregClient: EregClient

    @Autowired
    lateinit var server: MockRestServiceServer

    @Test
    fun `henter kommunenummer fra ereg`() {
        val virksomhetsnummer = "42"
        server.expect(requestTo("/v1/organisasjon/$virksomhetsnummer?inkluderHierarki=true"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(underenhetRespons, APPLICATION_JSON))

        val result = eregClient.hentKommunenummer(virksomhetsnummer)

        assertEquals("3801", result)
    }

    @Test
    fun `throws når underenhet er null fra ereg`() {
        val virksomhetsnummer = "42"
        server.expect(requestTo("/v1/organisasjon/$virksomhetsnummer?inkluderHierarki=true"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND).body(underenhetIkkeFunnetRespons).contentType(APPLICATION_JSON))


        assertThrows(HttpClientErrorException.NotFound::class.java) {
            eregClient.hentKommunenummer(virksomhetsnummer)
        }
    }
}

private const val underenhetRespons = """
{
  "organisasjonsnummer": "910825526",
  "type": "Virksomhet",
  "navn": {
    "navnelinje1": "GAMLE FREDRIKSTAD OG RAMNES REGNSKA",
    "navnelinje2": "P",
    "bruksperiode": {
      "fom": "2020-09-03T09:00:32.733"
    },
    "gyldighetsperiode": {
      "fom": "2020-09-03"
    }
  },
  "organisasjonDetaljer": {
    "registreringsdato": "2019-07-11T00:00:00",
    "enhetstyper": [
      {
        "enhetstype": "BEDR",
        "bruksperiode": {
          "fom": "2019-07-11T11:59:24.72"
        },
        "gyldighetsperiode": {
          "fom": "2019-07-11"
        }
      }
    ],
    "navn": [
      {
        "navnelinje1": "GAMLE FREDRIKSTAD OG RAMNES REGNSKA",
        "navnelinje2": "P",
        "bruksperiode": {
          "fom": "2020-09-03T09:00:32.733"
        },
        "gyldighetsperiode": {
          "fom": "2020-09-03"
        }
      }
    ],
    "forretningsadresser": [
      {
        "type": "Forretningsadresse",
        "adresselinje1": "AVDELING HORTEN, VED PHILIP LUNDQUI",
        "adresselinje2": "ST, APOTEKERGATA 16",
        "postnummer": "3187",
        "poststed": "HORTEN",
        "landkode": "NO",
        "kommunenummer": "3801",
        "bruksperiode": {
          "fom": "2020-09-03T09:00:32.693"
        },
        "gyldighetsperiode": {
          "fom": "2020-09-03"
        }
      }
    ],
    "postadresser": [
      {
        "type": "Postadresse",
        "adresselinje1": "PERSONALKONTORET, PHILIP LUNDQUIST,",
        "adresselinje2": "POSTBOKS 144",
        "postnummer": "4358",
        "poststed": "KLEPPE",
        "landkode": "NO",
        "kommunenummer": "1120",
        "bruksperiode": {
          "fom": "2020-09-03T09:00:32.685"
        },
        "gyldighetsperiode": {
          "fom": "2020-09-03"
        }
      }
    ],
    "navSpesifikkInformasjon": {
      "erIA": false,
      "bruksperiode": {
        "fom": "1900-01-01T00:00:00"
      },
      "gyldighetsperiode": {
        "fom": "1900-01-01"
      }
    },
    "sistEndret": "2020-09-03"
  },
  "virksomhetDetaljer": {
    "enhetstype": "BEDR"
  },
  "inngaarIJuridiskEnheter": [
    {
      "organisasjonsnummer": "810825472",
      "navn": {
        "navnelinje1": "MALMEFJORD OG RIDABU REGNSKAP",
        "bruksperiode": {
          "fom": "2020-05-14T16:03:21.12"
        },
        "gyldighetsperiode": {
          "fom": "2020-05-14"
        }
      },
      "bruksperiode": {
        "fom": "2020-09-03T09:00:32.718"
      },
      "gyldighetsperiode": {
        "fom": "2020-09-03"
      }
    }
  ]
}
"""

private const val underenhetIkkeFunnetRespons = """
{"melding": "Ingen organisasjon med organisasjonsnummer 910825674 ble funnet"}
"""

