package org.foxfairy.base.api.core.module;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicCodeExecutor {

    private final Map<String, Class<?>> compiledClassMap = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> instanceMap = new ConcurrentHashMap<>();

    private final static String TMP_DIR = "java.io.tmpdir";


    @Resource
    private CodeChecker codeChecker;
    /**
     *  动态运行类
     *  1. 如果 class 不变，则不更新，如果有变动，则更新
     *  2. 同理如果 class 变动，则伴随的 实例instance 也需要更新
     * @param className 类名
     * @param methodName 方法名
     * @param code 实例代码
     */
    public void execute(String className, String methodName, String code) {

        String sourceCode = generateSourceCode(className, methodName, code);
        if(codeChecker.isCodeSafe(sourceCode)){
            return;
        }

        String key = className + "." + methodName;
        Class<?> clazz = compiledClassMap.get(key);

        if (clazz == null) {
            clazz = compileAndLoadClass(className, sourceCode);
            compiledClassMap.put(key, clazz);
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            invokeMethod(instance, methodName);
        } catch (Exception e) {
            throw new RuntimeException("Error creating instance or invoking method: " + e.getMessage(), e);
        }
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

        // 保存源代码到临时文件
        String fileName = className + ".java";
        String filePath = System.getProperty(TMP_DIR) + File.separator + fileName;
        saveSourceCodeToFile(filePath, sourceCode);

        // 编译Java类
        int compilationResult = compiler.run(null, null, null, "-Xlint:-options", filePath);
        if (compilationResult != 0) {
            throw new RuntimeException("Compilation failed");
        }

        // 使用URLClassLoader加载编译后的类
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(System.getProperty(TMP_DIR)).toURI().toURL()});
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

    @SneakyThrows
    private void saveSourceCodeToFile(String filePath, String sourceCode) {
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
            writer.println(sourceCode);
        }
    }

    @Data
    static class ClassProperty{
        /**
         * 类名
         */
        private String className;
        /**
         * 实例
         */
        private Object instance;
        /**
         * 方法列表
         */
        private List<MethodProperty> methodPropertyList;
    }

    @Data
    static class MethodProperty{
        /**
         * 方法名
         */
        private String methodName;
        /**
         * code
         */
        private String sourceCode;
    }
}
