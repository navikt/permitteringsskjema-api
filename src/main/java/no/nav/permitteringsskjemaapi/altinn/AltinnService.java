package no.nav.permitteringsskjemaapi.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.permitteringsskjemaapi.exceptions.PermitteringsApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

import static no.nav.permitteringsskjemaapi.altinn.AltinnCacheConfig.ALTINN_CACHE;
import static no.nav.permitteringsskjemaapi.altinn.AltinnCacheConfig.ALTINN_TJENESTE_CACHE;

@Slf4j
@Component
public class AltinnService {

        private static final int ALTINN_ORG_PAGE_SIZE = 500;
        private final RestTemplate restTemplate;
        private final HttpEntity<String> headerEntity;
        private final String altinnUrl;

        @Autowired
        public AltinnService(AltinnConfig altinnConfig, RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            this.altinnUrl = altinnConfig.getAltinnurl();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NAV-APIKEY", altinnConfig.getAPIGwHeader());
            headers.set("APIKEY", altinnConfig.getAltinnHeader());
            this.headerEntity = new HttpEntity<>(headers);
        }

        @Cacheable(ALTINN_CACHE)
        public List<AltinnOrganisasjon> hentOrganisasjoner(String fnr) {
            System.out.println("hhent orgs "+ fnr);
            String query = "&subject=" + fnr
                    + "&$filter=(Type+eq+'Bedrift'+or+Type+eq+'Business'+or+Type+eq+'Enterprise'+or+Type+eq+'Foretak')+and+Status+eq+'Active'";
            String url = altinnUrl + "reportees/?ForceEIAuthentication" + query;
            log.info("Henter organisasjoner fra Altinn");
            return getFromAltinn(new ParameterizedTypeReference<List<AltinnOrganisasjon>>() {},url, ALTINN_ORG_PAGE_SIZE);
        }


        @Cacheable(ALTINN_TJENESTE_CACHE)
        public List<AltinnOrganisasjon> hentOrganisasjonerBasertPaRettigheter(String fnr, String serviceKode, String serviceEdition) {
            String query = "&subject=" + fnr
                    + "&serviceCode=" + serviceKode
                    + "&serviceEdition=" + serviceEdition;
            String url = altinnUrl + "reportees/?ForceEIAuthentication" + query;
            log.info("Henter rettigheter fra Altinn");
            return getFromAltinn(new ParameterizedTypeReference<List<AltinnOrganisasjon>>() {},url, ALTINN_ORG_PAGE_SIZE);
        }

        <T> List<T> getFromAltinn(ParameterizedTypeReference<List<T>> typeReference, String url, int pageSize) {
            Set<T> response = new HashSet<T>();
            int pageNumber = 0;
            boolean hasMore = true;
            while (hasMore) {
                pageNumber++;
                try {
                    String urlWithPagesizeAndOffset = url + "&$top=" + pageSize + "&$skip=" + ((pageNumber-1) * pageSize);
                    ResponseEntity<List<T>> exchange = restTemplate.exchange(urlWithPagesizeAndOffset, HttpMethod.GET, headerEntity, typeReference);
                    List<T> currentResponseList = exchange.getBody();
                    response.addAll(currentResponseList);
                    hasMore = currentResponseList.size() >= pageSize;
                } catch (RestClientException exception) {
                    log.error("Feil fra Altinn med spørring: " + url + " Exception: " + exception.getMessage());
                    throw new PermitteringsApiException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
                }
            }
            return new ArrayList<T>(response);
        }

    }
