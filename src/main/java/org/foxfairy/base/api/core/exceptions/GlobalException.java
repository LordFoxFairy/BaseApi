package org.foxfairy.base.api.core.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.common.HttpResponse;
import org.foxfairy.base.api.core.common.ResponseEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.rmi.ServerException;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    /**
     * 业务异常
     */
    @ExceptionHandler(ServerException.class)
    public HttpResponse<BaseException>  businessExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e)
    {
        log.error("[GlobalException][businessExceptionHandler] exception",e);
        return HttpResponse.error(ResponseEnum.SERVER_ERROR.getCode(), e.getMessage());
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public HttpResponse<BaseException> exceptionHandler(HttpServletRequest request,HttpServletResponse response,Exception e)
    {
        log.error("[GlobalException][exceptionHandler] exception",e);
        return HttpResponse.error(ResponseEnum.SERVER_ERROR.getCode(), e.getMessage());
    }
}