package no.nav.permitteringsskjemaapi.config

import com.fasterxml.jackson.core.JsonStreamContext
import net.logstash.logback.mask.ValueMasker
import java.util.regex.Pattern

class LogMasker : ValueMasker {
    private val orgnrPattern = Pattern.compile("\\b\\d{9}\\b")
    private val fnrPattern = Pattern.compile("\\b\\d{11}\\b")

    override fun mask(jsonStreamContext: JsonStreamContext, o: Any): Any? {
        return if (o is CharSequence) {
            maskFnr(maskOrgnr(o))
        } else null
    }

    private fun maskOrgnr(sequence: CharSequence): String {
        val matcher = orgnrPattern.matcher(sequence)
        return matcher.replaceAll("*********")
    }

    private fun maskFnr(sequence: CharSequence): String {
        val matcher = fnrPattern.matcher(sequence)
        return matcher.replaceAll("***********")
    }
}