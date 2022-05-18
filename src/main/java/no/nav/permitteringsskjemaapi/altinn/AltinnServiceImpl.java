package no.nav.permitteringsskjemaapi.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.nav.permitteringsskjemaapi.config.Constants.DEV_GCP;
import static no.nav.permitteringsskjemaapi.config.Constants.PROD_GCP;

@Slf4j
@Component
@Profile({DEV_GCP, PROD_GCP})
public class AltinnServiceImpl implements AltinnService {

    private static final int ALTINN_ORG_PAGE_SIZE = 500;
    private final RestTemplate restTemplate;
    private final String altinnProxyUrl;

    @Autowired
    public AltinnServiceImpl(AltinnConfig altinnConfig, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.altinnProxyUrl = altinnConfig.getAltinnProxyUrl();
    }

    public List<AltinnOrganisasjon> hentOrganisasjoner() {
        String query = "&$filter=Type+ne+'Person'+and+Status+eq+'Active'";
        return hentReporteesFraAltinn(query);
    }

    public List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter(String serviceKode, String serviceEdition) {
        String query = "&$filter=Type+ne+'Person'+and+Status+eq+'Active'"
                + "&serviceCode=" + serviceKode
                + "&serviceEdition=" + serviceEdition;
        return hentReporteesFraAltinn(query);
    }

    private List<AltinnOrganisasjon> hentReporteesFraAltinn(String query) {
        String baseUrl;
        baseUrl = altinnProxyUrl;

        String url = baseUrl + "reportees/?ForceEIAuthentication" + query;

        return getFromAltinn(new ParameterizedTypeReference<>() {
        }, url, ALTINN_ORG_PAGE_SIZE);
    }

    <T> List<T> getFromAltinn(
            ParameterizedTypeReference<List<T>> typeReference,
            String url,
            int pageSize
    ) {
        Set<T> response = new HashSet<T>();
        int pageNumber = 0;
        boolean hasMore = true;
        while (hasMore) {
            pageNumber++;
            try {
                String urlWithPagesizeAndOffset = url + "&$top=" + pageSize + "&$skip=" + ((pageNumber - 1) * pageSize);
                ResponseEntity<List<T>> exchange = restTemplate.exchange(urlWithPagesizeAndOffset, HttpMethod.GET, null, typeReference);
                List<T> currentResponseList = exchange.getBody();
                response.addAll(currentResponseList);
                hasMore = currentResponseList.size() >= pageSize;
            } catch (RestClientException exception) {
                log.error("Feil fra Altinn med spørring: : Exception:"  + exception.getMessage());
                throw new PermitteringsApiException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
            }
        }
        return new ArrayList<T>(response);
    }
}
