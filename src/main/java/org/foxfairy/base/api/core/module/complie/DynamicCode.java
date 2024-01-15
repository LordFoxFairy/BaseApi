package org.foxfairy.base.api.core.module.complie;

import cn.hutool.core.util.StrUtil;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import org.foxfairy.base.api.core.constant.DynamicConstant;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DynamicCode {
    protected ClassBuilder classBuilder;
    protected List<MethodBuilder> methodBuilders;
    protected Class<?> clazz;

    public static ClassBuilder classBuilder() {
        DynamicCode dynamicCode = new DynamicCode();
        return dynamicCode.getClassBuilder();
    }

    public MethodBuilder methodBuilder() {
        return new MethodBuilder(this);
    }

    private ClassBuilder getClassBuilder() {
        return new ClassBuilder(this);
    }


    public Class<?> clazz() {
        //        初始化包名和编译路径
        if (StrUtil.isBlank(this.classBuilder.packageName)) {
            this.classBuilder.packageName = DynamicConstant.DEFAULT_PACKAGE_PATH;
        }
        if (StrUtil.isBlank(this.classBuilder.compilerPath)) {
            this.classBuilder.compilerPath = DynamicConstant.DEFAULT_COMPILE_PATH;
        }
        //        拼接全限定类名
        String classFullName = this.classBuilder.packageName + "." + this.classBuilder.className;
        //        初始化类
        CtClass ctClass = JavassistUtil.createCtClass(classFullName);
        //        生成方法
        if (Objects.isNull(methodBuilders) || CollectionUtils.isEmpty(methodBuilders)) {
            return clazz;
        }
        List<CtMethod> methods = methodBuilders.stream()
                .map(method -> transformMethodBuilder(method, ctClass))
                .toList();
        JavassistUtil.appendMethod(ctClass, methods);
        try {
            clazz = ctClass.toClass(Thread.currentThread().getContextClassLoader(), null);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    public Object instance() {
        Class<?> selfClass = this.clazz();
        //        获取实例
        Object instance;
        try {
            instance = selfClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private CtMethod transformMethodBuilder(MethodBuilder methodBuilder, CtClass ctClass) {
        //        参数
        CtClass[] parameters = {};
        if (Objects.nonNull(methodBuilder.paramTypes)) parameters = methodBuilder.paramTypes.toArray(new CtClass[0]);
        //        返回值
        if (Objects.isNull(methodBuilder.returnType))
            methodBuilder.returnType = JavassistUtil.getClass("java.lang.Void");
        CtMethod method = new CtMethod(methodBuilder.returnType, methodBuilder.methodName, parameters, ctClass);
        try {
            methodBuilder.methodBody = StrUtil.isBlank(methodBuilder.methodBody)
                    ? "{}"
                    : methodBuilder.methodBody;
            methodBuilder.methodModifier = Arrays.stream(AccessFlag.values()).toList().contains(methodBuilder.methodModifier)
                    ? methodBuilder.methodModifier
                    : AccessFlag.PUBLIC;
            //            void时显示添加return;
            if (Objects.equals(methodBuilder.returnType, JavassistUtil.getClass("java.lang.Void"))
                    && !methodBuilder.methodBody.trim().endsWith("return;}")) {
                methodBuilder.methodBody = removeLastChart(methodBuilder.methodBody);
                methodBuilder.methodBody = appendSuffix(methodBuilder.methodBody, "return;}");
            }
            method.setBody(methodBuilder.methodBody);
            method.setModifiers(methodBuilder.methodModifier.mask());
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return method;
    }


    public static String removeLastChart(String main) {
        return main.substring(0, main.length() - 1);
    }

    public static String appendSuffix(String main, String suffix) {
        return main + suffix;
    }
}
