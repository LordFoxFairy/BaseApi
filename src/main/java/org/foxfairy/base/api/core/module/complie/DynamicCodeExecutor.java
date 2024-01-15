package org.foxfairy.base.api.core.module.complie;//package org.foxfairy.base.api.core.module;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.constant.DynamicConstant;
import org.foxfairy.base.api.core.entity.ClassProperty;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态执行类
 * 1. 获取类
 * 2. 编译类
 * 3. 生成类实例
 */
@Slf4j
@Component
public class DynamicCodeExecutor {

    private final ConcurrentMap<String, ClassProperty> classPropertyConcurrentHashMap = new ConcurrentHashMap<>();

    @Resource
    private CodeChecker codeChecker;

    public synchronized void execute(String className, String methodName, String code) {


        String sourceCode = generateSourceCode(code);

        if (!codeChecker.isCodeSafe(sourceCode)) {
            return;
        }

        String hashCode = codeChecker.generateHash(sourceCode);
        ClassProperty classProperty = classPropertyConcurrentHashMap.get(className);

        Object instance;
        if (Objects.nonNull(classProperty) && hashCode.equals(classProperty.getHashCode()) && Objects.nonNull(classProperty.getInstance())) {
            log.info("当前class已存在，无需重新创建");
            instance = classProperty.getInstance();
            invokeMethod(instance, methodName);
        } else {
            log.info("当前class不存在，正在创建中~");
            Class<?> clazz = compileAndLoadClass(DynamicConstant.DEFAULT_PACKAGE_PATH, className, sourceCode);
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
                ClassProperty newClassProperty = new ClassProperty();
                newClassProperty.setClazz(clazz);
                newClassProperty.setClassName(className);
                newClassProperty.setHashCode(hashCode);
                newClassProperty.setInstance(instance);
                classPropertyConcurrentHashMap.put(className, newClassProperty);

                invokeMethod(instance, methodName);
            } catch (Exception e) {
                throw new RuntimeException("Error creating instance or invoking method: " + e.getMessage(), e);
            }
        }
    }

    private String generateSourceCode(String className, String methodName, String code) {
        code = setPackage(code);
        return "public class " + className + " {\n" +
                "    public void " + methodName + "() {\n" +
                "        " + code + "\n" +
                "    }\n" +
                "}\n";
    }


    private String generateSourceCode(String className, String code) {
        code = setPackage(code);
        return "public class " + className + " {\n" +
                code +
                "}\n";
    }

    private String generateSourceCode(String code) {
        code = setPackage(code);
        return code;
    }
    
    public String setPackage(String code){
        String packageName = DynamicConstant.DEFAULT_PACKAGE_PATH;
        return "package " +
                packageName +
                ";" +
                code;
    }

    public Class<?> compileAndLoadClass(String packageName, String className, String sourceCode) {
        try {
            // 保存源代码到临时文件
            String fileName = className + ".java";
            Path filePath = saveSourceCodeToFile(packageName, fileName, sourceCode);

            // 编译Java类
            compileJavaClass(filePath);

            // 使用URLClassLoader加载编译后的类
            return loadCompiledClass(packageName, className);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error compiling and loading class " + packageName + "." + className, e);
        }
    }

    private Path saveSourceCodeToFile(String packageName, String fileName, String sourceCode) throws IOException {
        String packagePath = packageName.replace(".", File.separator);
        Path filePath = Path.of(DynamicConstant.DEFAULT_COMPILE_PATH, packagePath, fileName);

        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, sourceCode);

        return filePath;
    }

    private void compileJavaClass(Path filePath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // 编译Java类
        int compilationResult = compiler.run(null, null, null, "-Xlint:-options", filePath.toString());

        if (compilationResult != 0) {
            throw new RuntimeException("Compilation failed for file: " + filePath);
        }
    }

    private static Class<?> loadCompiledClass(String packageName, String className)
            throws ClassNotFoundException, MalformedURLException {
        // 构建类文件的URL
        File classFile = new File(DynamicConstant.DEFAULT_COMPILE_PATH + File.separator + packageName.replace(".", File.separator) + File.separator + className + ".class");
        URL classUrl = classFile.toURI().toURL();

        // 使用 URLClassLoader 加载类
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl}, ClassLoader.getSystemClassLoader())) {
            // 使用类加载器直接加载类
            return classLoader.loadClass(packageName + "." + className);
        } catch (Exception e) {
            throw new ClassNotFoundException("Error loading class: " + packageName + "." + className, e);
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
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
            writer.println(sourceCode);
        } catch (IOException e) {
            throw new RuntimeException("Error saving source code to file: " + e.getMessage(), e);
        }
    }
}