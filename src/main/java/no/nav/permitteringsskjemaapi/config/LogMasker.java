package no.nav.permitteringsskjemaapi.config;

import com.fasterxml.jackson.core.JsonStreamContext;
import net.logstash.logback.mask.ValueMasker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMasker implements ValueMasker {

    private final Pattern orgnrPattern = Pattern.compile("\\b\\d{9}\\b");
    private final Pattern fnrPattern = Pattern.compile("\\b\\d{11}\\b");

    @Override
    public Object mask(JsonStreamContext jsonStreamContext, Object o) {
        if (o instanceof CharSequence) {
            return maskFnr(maskOrgnr((CharSequence)o));
        }
        return null;
    }

    private String maskOrgnr(CharSequence sequence) {
        Matcher orgnrMatcher = orgnrPattern.matcher(sequence);
        return orgnrMatcher.replaceAll("*********");
    }

    private String maskFnr(CharSequence sequence) {
        Matcher orgnrMatcher = fnrPattern.matcher(sequence);
        return orgnrMatcher.replaceAll("***********");
    }


}
