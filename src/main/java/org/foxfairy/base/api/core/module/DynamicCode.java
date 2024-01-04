package org.foxfairy.base.api.core.module;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import org.foxfairy.base.api.core.constant.DynamicConstant;
import org.foxfairy.base.api.core.util.JavassistUtil;
import org.foxfairy.base.api.core.util.StringUtil;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DynamicCode {
    private ClassBuilder classBuilder;
    private List<MethodBuilder> methodBuilders;
    private Class<?> clazz;
    public static ClassBuilder classBuilder() {
        DynamicCode dynamicCode = new DynamicCode();
        return dynamicCode.getClassBuilder();
    }

    public MethodBuilder methodBuilder() {
        return this.new MethodBuilder();
    }

    private ClassBuilder getClassBuilder() {
        return new ClassBuilder();
    }


    public class ClassBuilder {
        private String packageName;
        private String className;
        private String compilerPath;

        public ClassBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public ClassBuilder className(String className) {
            this.className = className;
            return this;
        }

        public ClassBuilder compilerPath(String compilerPath) {
            this.compilerPath = compilerPath;
            return this;
        }

        public DynamicCode then(){
            DynamicCode.this.classBuilder = this;
            return DynamicCode.this;
        }
    }

    public class MethodBuilder {
        private AccessFlag methodModifier;
        private String methodName;
        private CtClass returnType;
        private List<CtClass> paramTypes;
        private String methodBody;

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
            if (Objects.isNull(DynamicCode.this.methodBuilders)) DynamicCode.this.methodBuilders = new ArrayList<>();
            DynamicCode.this.methodBuilders.add(this);
            return DynamicCode.this;
        }
    }

    public Class<?> clazz(){
//        初始化包名和编译路径
        if (StringUtil.isBlank(this.classBuilder.packageName)) this.classBuilder.packageName = DynamicConstant.DEFAULT_PACKAGE_PATH;
        if (StringUtil.isBlank(this.classBuilder.compilerPath)) this.classBuilder.compilerPath = DynamicConstant.DEFAULT_COMPILE_PATH;
//        拼接全限定类名
        String classFullName = this.classBuilder.packageName + "." + this.classBuilder.className;
//        初始化类
        CtClass ctClass = JavassistUtil.createCtClass(classFullName);
//        生成方法
        if (Objects.isNull(methodBuilders) || CollectionUtils.isEmpty(methodBuilders)) return clazz;
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

    public Object instance(){
        Class<?> selfClass = this.clazz();
//        获取实例
        Object instance = null;
        try {
            instance = selfClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private CtMethod transformMethodBuilder(MethodBuilder methodBuilder, CtClass ctClass) {
//        参数
        CtClass[] parameters = {};
        if (Objects.nonNull(methodBuilder.paramTypes)) parameters = methodBuilder.paramTypes.toArray(new CtClass[0]);
//        返回值
        if (Objects.isNull(methodBuilder.returnType)) methodBuilder.returnType = JavassistUtil.getClass("java.lang.Void");
        CtMethod method = new CtMethod(methodBuilder.returnType , methodBuilder.methodName, parameters, ctClass);
        try {
            methodBuilder.methodBody = StringUtil.isBlank(methodBuilder.methodBody)
                    ? "{}"
                    : methodBuilder.methodBody;
            methodBuilder.methodModifier = Arrays.stream(AccessFlag.values()).toList().contains(methodBuilder.methodModifier)
                    ? methodBuilder.methodModifier
                    : AccessFlag.PUBLIC;
//            void时显示添加return;
            if(Objects.equals(methodBuilder.returnType, JavassistUtil.getClass("java.lang.Void"))
                    && !methodBuilder.methodBody.trim().endsWith("return;}")) {
                methodBuilder.methodBody = StringUtil.removeLastChart(methodBuilder.methodBody);
                methodBuilder.methodBody = StringUtil.appendSuffix(methodBuilder.methodBody, "return;}");
            }
            method.setBody(methodBuilder.methodBody);
            method.setModifiers(methodBuilder.methodModifier.mask());
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return method;
    }
}
