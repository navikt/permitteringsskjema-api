package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver

import no.nav.permitteringsskjemaapi.config.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service

@Service
@ConditionalOnMissingBean(PermitteringsskjemaProdusent::class)
class ArbeidsgiverRapportLoggingProdusent : Arbeidsgiver {
    private val topic = "permittering-og-nedbemanning.aapen-permittering-arbeidsgiver"
    private val log = logger()

    override fun publiser(rapport: ArbeidsgiverRapport?) {
        log.info("Sender {} på {}", rapport, topic)
    }
}