package no.nav.permitteringsskjemaapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Configuration
class MDCConfig {
    /**
     * propagerer callid fra MDC til request header
     */
    @Bean
    fun callIdRestTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate: RestTemplate ->
            restTemplate.interceptors.add(
                ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
                    MDC.get(CALL_ID)?.let {
                        request.headers.addIfAbsent(CALL_ID, it)
                        request.headers.addIfAbsent(NAV_CALL_ID, it) // brukes av dokarkiv
                        request.headers.addIfAbsent(X_CORRELATION_ID, it) // brukes av oppgave
                    }
                    execution.execute(request, body!!)
                }
            )
        }
    }

    /**
     * propagerer correlationId fra MDC til request header
     */
    @Bean
    fun correlationIdRestTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate: RestTemplate ->
            restTemplate.interceptors.add(
                ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
                    MDC.get(X_CORRELATION_ID)?.let {
                        request.headers.addIfAbsent(X_CORRELATION_ID, it) // brukes av oppgave
                    }
                    execution.execute(request, body!!)
                }
            )
        }
    }

    /**
     * propagerer callId, inkl varianter, fra request header til MDC, setter ny uuid hvis mangler.
     * propagerer også callid til response header
     */
    @Bean
    fun callIdTilMdcFilter(
        @Value("\${spring.application.name:permitteringsskjema-api}")
        applicationName: String
    ): OncePerRequestFilter {
        val kjenteHeaderNavn = listOf(
            "X-Request-ID",
            "X-Correlation-ID",
            CALL_ID,
            "call-id",
            "call_id",
            "x_callId"
        )
        return object : OncePerRequestFilter() {
            override fun doFilterInternal(
                request: HttpServletRequest,
                response: HttpServletResponse,
                chain: FilterChain
            ) {
                try {
                    val callId = kjenteHeaderNavn
                        .map { request.getHeader(it) }
                        .filter(Objects::nonNull)
                        .firstOrNull(String::isNotBlank) ?: UUID.randomUUID().toString()
                    MDC.put(CALL_ID, callId)
                    response.setHeader(CALL_ID, callId)
                    MDC.put(NAV_CONSUMER_ID, request.getHeader(NAV_CONSUMER_ID) ?: applicationName)
                    MDC.put(NAV_CALL_ID, request.getHeader(NAV_CALL_ID) ?: UUID.randomUUID().toString())
                    chain.doFilter(request, response)
                } finally {
                    MDC.remove(CALL_ID)
                }
            }
        }
    }
}