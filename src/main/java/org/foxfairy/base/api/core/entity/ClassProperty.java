package org.foxfairy.base.api.core.entity;

import lombok.Data;

@Data
public class ClassProperty{
    /**
     * 类名
     */
    private String className;
    /**
     * 类
     */
    private Class<?> clazz;
    /**
     * 用于判断是否 sourceCode是否有变化
     */
    private String hashCode;
    /*
        创建实例
     */
    private Object instance;
}
