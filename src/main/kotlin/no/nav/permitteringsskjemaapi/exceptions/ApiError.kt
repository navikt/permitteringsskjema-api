package no.nav.permitteringsskjemaapi.exceptions

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.permitteringsskjemaapi.config.NAV_CALL_ID
import no.nav.permitteringsskjemaapi.util.*
import org.slf4j.MDC
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatusCode
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiError internal constructor(val status: HttpStatusCode, t: Throwable?, objects: List<Any?>) {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()

    @JsonFormat(with = [JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED])
    val messages: List<String>
    val uuid: String?

    init {
        messages = messages(t, objects)
        uuid = MDC.get(NAV_CALL_ID)
    }

    override fun toString() =
        "${javaClass.simpleName}[status=$status, timestamp=$timestamp, messages=$messages, uuid=$uuid]"

    companion object {
        private fun messages(t: Throwable?, objects: List<Any?>): List<String> {
            val messages: MutableList<Any?> = ArrayList(objects)
            if (t != null) {
                messages.add(NestedExceptionUtils.getMostSpecificCause(t).message)
            }
            return messages.mapNotNull { it.toString() }
        }
    }
}