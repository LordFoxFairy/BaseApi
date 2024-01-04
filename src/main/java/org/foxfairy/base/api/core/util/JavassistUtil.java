package org.foxfairy.base.api.core.util;

import javassist.*;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class JavassistUtil {
    private final ClassPool classPool = ClassPool.getDefault();
    public CtClass createCtClass(String className) {
        CtClass clazz = classPool.makeClass(className);
        try {
//            默认无参构造方法
            CtConstructor constructor = new CtConstructor(new CtClass[]{}, clazz);
            constructor.setBody("{}");
            clazz.addConstructor(constructor);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    public CtClass appendMethod(CtClass clazz, CtMethod method){
        try {
            clazz.addMethod(method);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    public CtClass appendMethod(CtClass clazz, List<CtMethod> methods){
        for (CtMethod method : methods) {
            appendMethod(clazz, method);
        }
        return clazz;
    }

    public CtClass getClass(String className) {
        try {
            if (Objects.equals(className, "java.lang.Void")) return classPool.get(Void.TYPE.getName());
            return classPool.get(className);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
