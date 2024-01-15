package org.foxfairy.base.api.core.aspect;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.foxfairy.base.api.core.annotation.Loggable;
import org.foxfairy.base.api.core.module.sql.SpelExpressionResolver;
import org.springframework.stereotype.Component;

/**
 * 日志处理类
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect extends BaseAspect{

    @Resource
    private SpelExpressionResolver spelExpressionResolver;

    @Around("@annotation(loggable)")
    public Object aroundMethodExecution(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        // 方法名
        String methodName = joinPoint.getSignature().getName();
        //获取动态参数
        String params = spelExpressionResolver.generateKeyBySpEL(loggable.value(), joinPoint);

        super.before(methodName, params);
        Object result = joinPoint.proceed();
        super.after(methodName, result);

        return result;
    }
}
