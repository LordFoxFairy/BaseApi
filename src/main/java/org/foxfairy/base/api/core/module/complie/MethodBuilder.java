package org.foxfairy.base.api.core.module.complie;

import javassist.CtClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.lang.reflect.AccessFlag;

public class MethodBuilder {
    protected AccessFlag methodModifier;
    protected String methodName;
    protected CtClass returnType;
    protected List<CtClass> paramTypes;
    protected String methodBody;

    protected DynamicCode dynamicCode;

    public MethodBuilder(DynamicCode dynamicCode) {
        this.dynamicCode = dynamicCode;
    }

    public MethodBuilder methodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public MethodBuilder methodBody(String methodBody) {
        this.methodBody = methodBody;
        return this;
    }

    /**
     * 设置返回类型
     * @param className 全限定类名
     * @return MethodBuilder
     */
    public MethodBuilder returnType(String className) {
        this.returnType = JavassistUtil.getClass(className);
        return this;
    }

    public MethodBuilder appendParam(String className) {
        if (Objects.isNull(this.paramTypes)) this.paramTypes = new ArrayList<>();
        this.paramTypes.add(JavassistUtil.getClass(className));
        return this;
    }

    public DynamicCode then(){
        if (Objects.isNull(dynamicCode.methodBuilders)) dynamicCode.methodBuilders = new ArrayList<>();
        dynamicCode.methodBuilders.add(this);
        return dynamicCode;
    }
}
