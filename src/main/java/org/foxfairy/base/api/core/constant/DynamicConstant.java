package org.foxfairy.base.api.core.constant;

import java.io.File;

public record DynamicConstant() {
    /**
     * 默认包路径 - 单机使用的情况下为末尾使用 temp， 集群使用则为末尾使用 用户唯一标识符
     */
    public static final String DEFAULT_PACKAGE_PATH = "org.foxfairy.base.api.temp";
    /**
     * 默认编译路径 - 实际上就是项目编译文件默认存放地
     */
    public static final String DEFAULT_COMPILE_PATH = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
}
