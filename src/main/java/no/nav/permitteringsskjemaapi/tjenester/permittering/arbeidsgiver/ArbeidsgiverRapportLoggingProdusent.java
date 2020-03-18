package no.nav.permitteringsskjemaapi.tjenester.permittering.arbeidsgiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(ArbeidsgiverMeldingKafkaProdusent.class)
public class ArbeidsgiverRapportLoggingProdusent implements Arbeidsgiver {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsgiverRapportLoggingProdusent.class);
    private final ArbeidsgiverRapportConfig config;

    public ArbeidsgiverRapportLoggingProdusent(ArbeidsgiverRapportConfig config) {
        this.config = config;
    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        LOG.info("Sender {} på {}", rapport, config.getTopic());
    }
}
