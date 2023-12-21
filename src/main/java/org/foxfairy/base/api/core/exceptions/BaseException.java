package org.foxfairy.base.api.core.exceptions;

import lombok.Getter;
import org.foxfairy.base.api.core.common.IResponseEnum;

@Getter
public class BaseException extends Throwable {

    private final IResponseEnum responseEnum;

    private final String message;

    /**
     * 参数
     */
    private final Object[] args;

    private Throwable cause;

    public BaseException(IResponseEnum responseEnum, Object[] args, String message) {
        this.responseEnum = responseEnum;
        this.args = args;
        this.message = message;
    }

    public BaseException(IResponseEnum responseEnum, Object[] args, String message, Throwable cause) {
        this.responseEnum = responseEnum;
        this.args = args;
        this.message = message;
        this.cause = cause;
    }
}
