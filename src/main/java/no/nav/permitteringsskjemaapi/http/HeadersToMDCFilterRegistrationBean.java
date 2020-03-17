package no.nav.permitteringsskjemaapi.http;

import static no.nav.permitteringsskjemaapi.http.FilterRegistrationUtil.always;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Component;

@Component
public class HeadersToMDCFilterRegistrationBean extends FilterRegistrationBean<HeadersToMDCFilterBean> {
    private static final Logger LOG = LoggerFactory.getLogger(HeadersToMDCFilterRegistrationBean.class);

    public HeadersToMDCFilterRegistrationBean(HeadersToMDCFilterBean headersFilter) {
        setFilter(headersFilter);
        setUrlPatterns(always());
        LOG.info("Registrert filter {}", this.getClass().getSimpleName());
    }
}
