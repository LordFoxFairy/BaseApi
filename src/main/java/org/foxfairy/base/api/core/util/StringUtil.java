package org.foxfairy.base.api.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public String removeLastChart(String main){
        return main.substring(0, main.length() - 1);
    }
    public String appendSuffix(String main, String suffix){
        return main + suffix;
    }
}
