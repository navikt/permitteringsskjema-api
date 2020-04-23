package no.nav.permitteringsskjemaapi.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.foreldrepenger.boot.conditionals.Cluster;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnClusters;
import no.nav.foreldrepenger.boot.conditionals.ConditionalOnLocal;
import no.nav.permitteringsskjemaapi.exceptions.EnhetFinnesIkkeException;
import no.nav.permitteringsskjemaapi.exceptions.IkkeFunnetException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@ConditionalOnClusters(clusters = { Cluster.DEV_FSS, Cluster.PROD_FSS })
@ConditionalOnLocal
@Profile("wiremock")
public class EregServiceImpl implements EregService {

    @Value("${ereg.url}")
    private String eregUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public EregServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public EregOrganisasjon hentOrganisasjon(String orgnr) {
        String eregurMedParam = eregUrl + orgnr;
        try {
        ResponseEntity<EregOrganisasjon> response = restTemplate.exchange(eregurMedParam, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), EregOrganisasjon.class);
        return response.getBody();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
