package no.nav.permitteringsskjemaapi.exceptions

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.permitteringsskjemaapi.config.logger
import no.nav.permitteringsskjemaapi.util.TokenUtil
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.stream.Collectors

@ControllerAdvice
class PermitteringExceptionHandler(private val tokenUtil: TokenUtil) : ResponseEntityExceptionHandler() {
    private val log = logger()

    @ResponseBody
    @ExceptionHandler(
        HttpStatusCodeException::class
    )
    fun handleHttpStatusCodeException(e: HttpStatusCodeException, request: WebRequest): ResponseEntity<Any>? {
        return if (e.statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED) || e.statusCode.isSameCodeAs(HttpStatus.FORBIDDEN)) {
            logAndHandle(e.statusCode, e, request, messages = listOf(tokenUtil.expiryDate))
        } else
            logAndHandle(e.statusCode, e, request, messages = listOf<Any>())
    }

    override fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        req: WebRequest
    ): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.UNPROCESSABLE_ENTITY, e, req, headers, validationErrors(e))
    }

    override fun handleHttpMessageNotReadable(
        e: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        req: WebRequest
    ): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.UNPROCESSABLE_ENTITY, e, req, headers)
    }

    @ExceptionHandler(JsonProcessingException::class)
    fun handleJsonProcessingException(e: JsonProcessingException, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.UNPROCESSABLE_ENTITY, e, req)
    }

    @ExceptionHandler(PermitteringsApiException::class)
    fun apiException(e: PermitteringsApiException, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.UNPROCESSABLE_ENTITY, e, req)
    }

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(e: JwtTokenUnauthorizedException, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.UNAUTHORIZED, e, req)
    }

    @ExceptionHandler(JwtTokenValidatorException::class)
    fun handleUnauthenticatedJwtException(e: JwtTokenValidatorException, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.FORBIDDEN, e, req, messages = listOf(e.expiryDate))
    }

    @ExceptionHandler(value = [Exception::class])
    protected fun handleUncaught(e: Exception, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.INTERNAL_SERVER_ERROR, e, req)
    }

    @ExceptionHandler(value = [IkkeTilgangException::class])
    protected fun ikkeTilgang(e: Exception, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.FORBIDDEN, e, req)
    }

    @ExceptionHandler(value = [IkkeFunnetException::class])
    protected fun ikkeFunnet(e: Exception, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.NOT_FOUND, e, req)
    }

    @ExceptionHandler(value = [AlleFelterIkkeFyltUtException::class])
    protected fun alleFelterIkkeFyltUt(e: Exception, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.BAD_REQUEST, e, req)
    }

    @ExceptionHandler(value = [SkjemaErAvbruttException::class])
    protected fun avbrutt(e: Exception, req: WebRequest): ResponseEntity<Any>? {
        return logAndHandle(HttpStatus.BAD_REQUEST, e, req)
    }

    private fun logAndHandle(
        status: HttpStatusCode,
        e: Exception,
        req: WebRequest,
        headers: HttpHeaders = HttpHeaders(),
        messages: List<Any?> = emptyList(),
    ): ResponseEntity<Any>? {
        val apiError = ApiError(status, e, messages)
        log.warn("{} {} ({})", status, apiError.messages, status.value(), e)
        return handleExceptionInternal(e, apiError, headers, status, req)
    }

    companion object {
        private fun validationErrors(e: MethodArgumentNotValidException): List<String> {
            return e.bindingResult.fieldErrors
                .stream()
                .map { error: FieldError -> errorMessage(error) }
                .collect(Collectors.toList())
        }

        private fun errorMessage(error: FieldError): String {
            return error.field + " " + error.defaultMessage
        }
    }
}