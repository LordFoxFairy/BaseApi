package org.foxfairy.base.api.core.module.complie;

public class ClassBuilder {
    protected String packageName;
    protected String className;
    protected String compilerPath;

    protected DynamicCode dynamicCode;

    public ClassBuilder (DynamicCode dynamicCode){
        this.dynamicCode = dynamicCode;
    }

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
        this.dynamicCode.classBuilder = this;
        return this.dynamicCode;
    }
}
