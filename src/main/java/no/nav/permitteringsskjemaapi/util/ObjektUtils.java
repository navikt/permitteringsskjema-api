package no.nav.permitteringsskjemaapi.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

@UtilityClass
public class ObjektUtils {
    public static boolean isAnyEmpty(Object... objects) {
        for (Object object : objects) {
            if (ObjectUtils.isEmpty(object)) {
                return true;
            }
        }
        return false;
    }
}
