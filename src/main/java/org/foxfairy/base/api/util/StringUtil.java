package org.foxfairy.base.api.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
