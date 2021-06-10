package no.nav.permitteringsskjemaapi.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException;
import no.nav.permitteringsskjemaapi.featuretoggles.FeatureToggleService;
import no.nav.permitteringsskjemaapi.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.nav.permitteringsskjemaapi.config.Constants.DEV_FSS;
import static no.nav.permitteringsskjemaapi.config.Constants.PROD_FSS;

@Slf4j
@Component
@Profile({DEV_FSS, PROD_FSS})
public class AltinnServiceImpl implements AltinnService {

    private static final int ALTINN_ORG_PAGE_SIZE = 500;
    private final RestTemplate restTemplate;
    private final HttpEntity<HttpHeaders> headerEntity;
    private final String altinnUrl;
    private final String altinnProxyUrl;
    private final FeatureToggleService featureToggleService;
    private final TokenUtil tokenUtil;

    @Autowired
    public AltinnServiceImpl(AltinnConfig altinnConfig, RestTemplate restTemplate, FeatureToggleService featureToggleService, TokenUtil tokenUtil) {
        this.restTemplate = restTemplate;
        this.altinnUrl = altinnConfig.getAltinnurl();
        this.altinnProxyUrl = altinnConfig.getAltinnProxyUrl();
        this.featureToggleService = featureToggleService;
        this.tokenUtil = tokenUtil;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnConfig.getAPIGwHeader());
        headers.set("APIKEY", altinnConfig.getAltinnHeader());
        this.headerEntity = new HttpEntity<>(headers);
    }

    public List<AltinnOrganisasjon> hentOrganisasjoner(String fnr) {
        String query = "&$filter=Type+ne+'Person'+and+Status+eq+'Active'";
        return hentReporteesFraAltinn(query, fnr);
    }

    public List<AltinnOrganisasjon> hentOrganisasjonerBasertPåRettigheter(String fnr, String serviceKode, String serviceEdition) {
        String query = "&$filter=Type+ne+'Person'+and+Status+eq+'Active'"
                + "&serviceCode=" + serviceKode
                + "&serviceEdition=" + serviceEdition;
        return hentReporteesFraAltinn(query, fnr);
    }

    private List<AltinnOrganisasjon> hentReporteesFraAltinn(String query, String fnr) {
        String baseUrl;
        HttpEntity<HttpHeaders> headers;

        if (featureToggleService.isEnabled("arbeidsgiver.permitteringsskjema-api.bruk-altinn-proxy")) {
            baseUrl = altinnProxyUrl;
            headers = getAltinnProxyHeaders();
        } else {
            baseUrl = altinnUrl;
            headers = headerEntity;
            query += "&subject=" + fnr;
        }

        String url = baseUrl + "reportees/?ForceEIAuthentication" + query;

        return getFromAltinn(new ParameterizedTypeReference<>() {
        }, url, ALTINN_ORG_PAGE_SIZE, headers);
    }

    <T> List<T> getFromAltinn(
            ParameterizedTypeReference<List<T>> typeReference,
            String url,
            int pageSize,
            HttpEntity<HttpHeaders> headers
    ) {
        Set<T> response = new HashSet<T>();
        int pageNumber = 0;
        boolean hasMore = true;
        while (hasMore) {
            pageNumber++;
            try {
                String urlWithPagesizeAndOffset = url + "&$top=" + pageSize + "&$skip=" + ((pageNumber - 1) * pageSize);
                ResponseEntity<List<T>> exchange = restTemplate.exchange(urlWithPagesizeAndOffset, HttpMethod.GET, headers, typeReference);
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

    private HttpEntity<HttpHeaders> getAltinnProxyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtil.getTokenForInnloggetBruker());
        headers.set("x-consumer-id", "permitteringsskjema-api");
        return new HttpEntity<>(headers);
    }
}
