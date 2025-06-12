package no.nav.permitteringsskjemaapi.kafka

import kotlinx.coroutines.runBlocking
import no.nav.permitteringsskjemaapi.notifikasjon.ProdusentApiKlient
import no.nav.permitteringsskjemaapi.permittering.*
import no.nav.permitteringsskjemaapi.util.urlTilPermitteringsløsningFrontend
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant
import java.time.LocalDate
import java.util.*

@SpringBootTest(
    classes = [SkedulerPermitteringsmeldingService::class]
)
@ActiveProfiles("test")
class SkedulerPermitteringsmeldingServiceTest {

    @MockitoBean
    lateinit var permitteringsmeldingKafkaRepository: PermitteringsmeldingKafkaRepository

    @MockitoBean
    lateinit var permitteringsskjemaRepository: PermitteringsskjemaRepository

    @MockitoBean
    lateinit var permitteringsskjemaProdusent: PermitteringsskjemaProdusent

    @MockitoBean
    lateinit var produsentApiKlient: ProdusentApiKlient

    @Autowired
    lateinit var service: SkedulerPermitteringsmeldingService

    private val skjema = Permitteringsskjema(
        id = UUID.randomUUID(),
        antallBerørt = 1,
        bedriftNavn = "hey",
        bedriftNr = "1234567890",
        kontaktEpost = "hey",
        kontaktNavn = "hey",
        kontaktTlf = "hey",
        opprettetAv = "hey",
        sendtInnTidspunkt = Instant.parse("2010-01-01T01:01:01Z"),
        startDato = LocalDate.parse("2020-01-01"),
        sluttDato = LocalDate.parse("2020-01-01"),
        ukjentSluttDato = false,
        type = SkjemaType.INNSKRENKNING_I_ARBEIDSTID,
        yrkeskategorier = listOf(Yrkeskategori(1, "hey", "hey")),
        årsakskode = Årsakskode.MANGEL_PÅ_ARBEID,
    )

    private val queueItem = PermitteringsmeldingKafkaEntry(skjema.id)


    @Test
    fun `if opprettNySak fails, opprettNyBeskjed is not called`() = runBlocking {
        `when`(
            permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100))
        ).thenReturn(listOf(queueItem))

        `when`(
            permitteringsskjemaRepository.findById(skjema.id)
        ).thenReturn(skjema)

        `when`(
            produsentApiKlient.opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )
        ).thenThrow(RuntimeException("opprettNySak feilet"))

        assertThrows<RuntimeException> {
            service.scheduleFixedRateTask()
        }

        verify(produsentApiKlient, times(1))
            .opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )

        verify(produsentApiKlient, never())
            .opprettNyBeskjed(
                grupperingsid = anyString(),
                merkelapp = anyString(),
                virksomhetsnummer = anyString(),
                tekst = anyString(),
                lenke = anyString(),
                tidspunkt = any()
            )

        verify(permitteringsmeldingKafkaRepository, never()).delete(org.mockito.kotlin.any())
        verify(permitteringsskjemaProdusent, never()).sendTilKafkaTopic(org.mockito.kotlin.any())
    }

    @Test
    fun `if opprettNyBeskjed fails, queue item is not deleted and is retried`() = runBlocking {
        `when`(
            permitteringsmeldingKafkaRepository.fetchQueueItems(Pageable.ofSize(100))
        ).thenReturn(listOf(queueItem))

        `when`(
            permitteringsskjemaRepository.findById(skjema.id)
        ).thenReturn(skjema)


        // First run succeeds on opprettNySak, but fails on opprettNyBeskjed
        `when`(
            produsentApiKlient.opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )
        ).thenReturn(Unit)
        `when`(
            produsentApiKlient.opprettNyBeskjed(
                tekst = skjema.type.beskjedTekst,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )
        ).thenThrow(RuntimeException("opprettNyBeskjed feilet"))

        assertThrows<RuntimeException> {
            service.scheduleFixedRateTask()
        }

        verify(produsentApiKlient, times(1))
            .opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )

        verify(produsentApiKlient, times(1))
            .opprettNyBeskjed(
                tekst = skjema.type.beskjedTekst,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )

        verify(permitteringsmeldingKafkaRepository, never()).delete(org.mockito.kotlin.any())
        verify(permitteringsskjemaProdusent, never()).sendTilKafkaTopic(org.mockito.kotlin.any())

        // retry kalls both opprettNySak and opprettNyBeskjed and is successful
        reset(produsentApiKlient)
        `when`(
            produsentApiKlient.opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )
        ).thenReturn(Unit)
        `when`(
            produsentApiKlient.opprettNyBeskjed(
                tekst = skjema.type.beskjedTekst,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )
        ).thenReturn(Unit)

        service.scheduleFixedRateTask()

        verify(produsentApiKlient, times(1))
            .opprettNySak(
                tittel = skjema.type.tittel,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )

        verify(produsentApiKlient, times(1))
            .opprettNyBeskjed(
                tekst = skjema.type.beskjedTekst,
                grupperingsid = skjema.id.toString(),
                merkelapp = skjema.type.merkelapp,
                virksomhetsnummer = skjema.bedriftNr,
                lenke = "$urlTilPermitteringsløsningFrontend${skjema.id}",
                tidspunkt = skjema.sendtInnTidspunkt.toString()
            )

        verify(permitteringsmeldingKafkaRepository, times(1)).delete(queueItem)
        verify(permitteringsskjemaProdusent, times(1)).sendTilKafkaTopic(skjema)
    }

}
