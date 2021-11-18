package no.nav.permitteringsskjemaapi.config;

import static java.lang.System.getenv;
import static no.nav.permitteringsskjemaapi.config.Constants.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClusterAwareSpringProfileResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterAwareSpringProfileResolver.class);

    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    private ClusterAwareSpringProfileResolver() {

    }

    public static String[] profiles() {
        return Optional.ofNullable(profilFra(getenv(NAIS_CLUSTER_NAME)))
                .map(c -> new String[] { c })
                .orElse(new String[0]);
    }

    private static String profilFra(String cluster) {
        if (cluster == null) {
            LOG.info("{} ikke satt, setter til {}", NAIS_CLUSTER_NAME, LOCAL);
            System.setProperty(NAIS_CLUSTER_NAME, LOCAL);
            return LOCAL;
        }
        LOG.info("Aktivt cluster er {}", cluster);
        if (cluster.equals(DEV_GCP)) {
            return DEV_GCP;
        }

        if (cluster.equals(PROD_GCP)) {
            return PROD_GCP;
        }
        return DEFAULT;
    }
}
