package org.foxfairy.base.api.core.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.foxfairy.base.api.core.exception.BusinessExceptionAssert;

/**
 * 对应错误码和错误内容
 */
@Getter
@AllArgsConstructor
public enum ResponseEnum implements BusinessExceptionAssert {

    SERVER_ERROR(500, "服务异常"),

    /**
     * Bad licence type
     */
    BAD_LICENCE_TYPE(7001, "Bad licence type."),
    /**
     * Licence not found
     */
    LICENCE_NOT_FOUND(7002, "Licence not found.");



    /**
     * 返回码
     */
    private final int code;
    /**
     * 返回消息
     */
    private final String message;
}
