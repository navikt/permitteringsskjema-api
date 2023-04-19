package no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(PermitteringsskjemaProdusent.class)
public class ArbeidsgiverRapportLoggingProdusent implements Arbeidsgiver {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsgiverRapportLoggingProdusent.class);
    private final String topic;

    public ArbeidsgiverRapportLoggingProdusent(@Value("${kafka.topic}") String topic) {
        this.topic = topic;
    }

    @Override
    public void publiser(ArbeidsgiverRapport rapport) {
        LOG.info("Sender {} på {}", rapport, topic);
    }
}
