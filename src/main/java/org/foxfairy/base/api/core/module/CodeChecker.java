package org.foxfairy.base.api.core.module;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CodeChecker {
    private List<Pattern> whitelistPackages;
    private List<Pattern> blacklistPackages;

    public CodeChecker() {
        whitelistPackages = new ArrayList<>();
        blacklistPackages = new ArrayList<>();

        // 设置默认的黑名单
        addBlacklistPackage("java.io.*");
        addBlacklistPackage("java.nio.*");
    }

    public void addBlacklistPackage(String packageName) {
        blacklistPackages.add(Pattern.compile(packageName));
    }

    public boolean isCodeSafe(String code) {
        String[] lines = code.split("\n");

        for (String line : lines) {
            if (line.trim().startsWith("import")) {
                String importedPackage = line.trim().substring("import".length()).trim();
                if (isBlacklistedPackage(importedPackage)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBlacklistedPackage(String packageName) {
        for (Pattern blacklistPackage : blacklistPackages) {
            if (blacklistPackage.matcher(packageName).matches()) {
                return true;
            }
        }
        return false;
    }
}
