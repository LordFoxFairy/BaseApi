package org.foxfairy.base.api.core.aspect;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.foxfairy.base.api.core.annotations.Loggable;
import org.foxfairy.base.api.core.module.SpelExpressionResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Resource
    private SpelExpressionResolver spelExpressionResolver;

    @Around("@annotation(loggable)")
    public Object logAroundMethodExecution(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        Object result;
        // 方法名
        String methodName = joinPoint.getSignature().getName();
        //获取动态参数
        String params = spelExpressionResolver.generateKeyBySpEL(loggable.value(), joinPoint);
        log.info("获取动态aop参数: {}", params);
        log.info("执行目标方法: {} ==>>开始......", methodName);
        result = joinPoint.proceed();
        log.info("执行目标方法: {} ==>>结束......", methodName);
        // 返回通知
        log.info("目标方法 " + methodName + " 执行结果 " + result);
        // 后置通知
        log.info("目标方法 " + methodName + " 结束");
        return result;
    }
}
