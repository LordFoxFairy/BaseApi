package org.foxfairy.base.api.core.module;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.entity.ClassProperty;
import org.springframework.stereotype.Component;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class DynamicCodeExecutor {

    private final ConcurrentMap<String, ClassProperty> compiledClasses = new ConcurrentHashMap<>();

    @Resource
    private CodeChecker codeChecker;

    public synchronized void execute(String className, String methodName, String code) {

        String sourceCode = generateSourceCode(className, methodName, code);
        if(!codeChecker.isCodeSafe(sourceCode)){
            return ;
        }

        String hashCode = codeChecker.generateHash(sourceCode);
        ClassProperty classProperty = compiledClasses.get(className);

        Object instance;
        if(Objects.nonNull(classProperty) && hashCode.equals(classProperty.getHashCode()) && Objects.nonNull(classProperty.getInstance())){
            log.info("当前class已存在，无需重新创建");
            instance = classProperty.getInstance();
            invokeMethod(instance, methodName);
        }else{
            log.info("当前class不存在，正在创建中~");
            Class<?> clazz = compileAndLoadClass(className, sourceCode);
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
                ClassProperty newClassProperty = new ClassProperty();
                newClassProperty.setClazz(clazz);
                newClassProperty.setClassName(className);
                newClassProperty.setHashCode(hashCode);
                newClassProperty.setInstance(instance);
                compiledClasses.put(className, newClassProperty);

                invokeMethod(instance, methodName);
            } catch (Exception e) {
                throw new RuntimeException("Error creating instance or invoking method: " + e.getMessage(), e);
            }
        }
        Class<?> clazz = compiledClasses.get(className).getClazz();
        String classPath = clazz
                .getResource(clazz.getSimpleName() + ".class")
                .getPath().replaceAll("/" + clazz.getSimpleName() + ".class$", "")
                .replaceAll("^file:", "").replaceAll("%20", " ");
        log.info("当前class的全局类路径：{}", classPath);
    }

    private String generateSourceCode(String className, String methodName, String code) {

        return "public class " + className + " {\n" +
                "    public void " + methodName + "() {\n" +
                "        " + code + "\n" +
                "    }\n" +
                "}\n";
    }

    private Class<?> compileAndLoadClass(String className, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // 保存源代码到临时文件
        String fileName = className + ".java";
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
        saveSourceCodeToFile(filePath, sourceCode);

        // 编译Java类
//        int compilationResult = compiler.run(null, null, null, filePath);
        int compilationResult = compiler.run(null, null, null, "-Xlint:-options", filePath);

        if (compilationResult != 0) {
            throw new RuntimeException("Compilation failed");
        }

        // 使用URLClassLoader加载编译后的类
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(System.getProperty("java.io.tmpdir")).toURI().toURL()});
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Error loading class " + className, e);
        }
    }

    private void invokeMethod(Object instance, String methodName) {
        try {
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method " + methodName + ": " + e.getMessage(), e);
        }
    }

    private void saveSourceCodeToFile(String filePath, String sourceCode) {
        try (PrintWriter writer = new PrintWriter(filePath, "UTF-8")) {
            writer.println(sourceCode);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error saving source code to file: " + e.getMessage(), e);
        }
    }
}
