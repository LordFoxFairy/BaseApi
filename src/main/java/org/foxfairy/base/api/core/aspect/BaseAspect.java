package org.foxfairy.base.api.core.aspect;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseAspect {
    public void before(String methodName, String params){
        log.info("获取动态aop参数: {}", params);
        log.info("执行目标方法: {} ==>>开始......", methodName);
    }

    public void after(String methodName, Object result){
        log.info("执行目标方法: {} ==>>结束......", methodName);
        // 返回通知
        log.info("目标方法 " + methodName + " 执行结果 " + result);
        // 后置通知
        log.info("目标方法 " + methodName + " 结束");
    }
}
