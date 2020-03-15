package no.nav.permitteringsskjemaapi.tjenester.permittering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import no.nav.permitteringsskjemaapi.Permitteringsskjema;

@Service
@ConditionalOnProperty(name = "permittering.enabled", havingValue = "false")
public class PermitteringMeldingLoggingProdusent implements Permittering {

    private static final Logger LOG = LoggerFactory.getLogger(PermitteringMeldingLoggingProdusent.class);

    @Override
    public void publiser(Permitteringsskjema skjema) {
        LOG.info("Sender {}", skjema);

    }
}
