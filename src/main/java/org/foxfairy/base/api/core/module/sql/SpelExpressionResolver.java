package org.foxfairy.base.api.core.module.sql;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
@Component
public class SpelExpressionResolver {

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 用于获取方法参数定义名字.
     */
    private static final DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 解析SpEL表达式
     */
    public String generateKeyBySpEL(String spELStr, ProceedingJoinPoint joinPoint) {
        // 通过joinPoint获取被注解方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 使用Spring的DefaultParameterNameDiscoverer获取方法形参名数组
        String[] paramNames = defaultParameterNameDiscoverer.getParameterNames(method);
        // 解析过后的Spring表达式对象
        Expression expression = parser.parseExpression(spELStr);
        // Spring的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 通过joinPoint获取被注解方法的形参
        Object[] args = joinPoint.getArgs();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            if (paramNames != null) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return Objects.requireNonNull(expression.getValue(context)).toString();
    }
}