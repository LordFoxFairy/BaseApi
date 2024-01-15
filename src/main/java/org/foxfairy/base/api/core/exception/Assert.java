package org.foxfairy.base.api.core.exception;

public interface Assert {
    /**
     * 创建异常
     */
    BaseException newException(Object... args);

    /**
     * 创建异常
     */
    BaseException newException(Throwable t, Object... args);

    /**
     * 断言对象 obj 非空。如果对象 obj 为空，则抛出异常
     */
    default void assertNotNull(Object obj) throws BaseException {
        if (obj == null) {
            throw newException((Object) null);
        }
    }

    /**
     * 断言对象 obj 非空。如果对象 obj 为空，则抛出异常
     * 异常信息 message 支持传递参数方式，避免在判断之前进行字符串拼接操作
     *
     * @param obj 待判断对象
     * @param args message占位符对应的参数列表
     */
    default void assertNotNull(Object obj, Object... args) throws BaseException {
        if (obj == null) {
            throw newException(args);
        }
    }
}
