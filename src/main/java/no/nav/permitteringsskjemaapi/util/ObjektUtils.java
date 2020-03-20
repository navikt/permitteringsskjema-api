package no.nav.permitteringsskjemaapi.util;

import java.util.Arrays;

import org.apache.commons.lang3.ObjectUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjektUtils {
    public static boolean isAnyEmpty(Object... objects) {
        return Arrays.stream(objects)
                .filter(ObjectUtils::isEmpty)
                .findFirst()
                .isPresent();
    }
}
