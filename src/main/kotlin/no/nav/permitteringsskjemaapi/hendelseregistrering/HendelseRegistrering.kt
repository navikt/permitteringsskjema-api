package no.nav.permitteringsskjemaapi.hendelseregistrering

import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.permittering.Permitteringsskjema
import org.springframework.stereotype.Component

@Component
class HendelseRegistrering(
    private val repository: HendelseRepository
) {
    private val log = logger()

    fun opprettet(permitteringsskjema: Permitteringsskjema, utførtAv: String) {
        val skjemaId = permitteringsskjema.id!!
        val hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.OPPRETTET, utførtAv))
        log.info("Skjema opprettet skjemaId={} hendelseId={}", skjemaId, hendelse.id)
    }

    fun endret(permitteringsskjema: Permitteringsskjema, utførtAv: String) {
        val skjemaId = permitteringsskjema.id!!
        val hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.ENDRET, utførtAv))
        log.info("Skjema endret skjemaId={} hendelseId={}", skjemaId, hendelse.id)
    }

    fun sendtInn(permitteringsskjema: Permitteringsskjema, utførtAv: String) {
        val skjemaId = permitteringsskjema.id!!
        val hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.SENDT_INN, utførtAv))
        log.info("Skjema sendt inn skjemaId={} hendelseId={}", skjemaId, hendelse.id)
    }

    fun avbrutt(permitteringsskjema: Permitteringsskjema, utførtAv: String) {
        val skjemaId = permitteringsskjema.id!!
        val hendelse = repository.save(Hendelse.nyHendelse(skjemaId, HendelseType.AVBRUTT, utførtAv))
        log.info("Skjema avbrutt skjemaId={} hendelseId={}", skjemaId, hendelse.id)
    }
}