package org.foxfairy.base.api;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.foxfairy.base.api.core.module.DynamicCodeExecutor;
import org.foxfairy.base.api.core.module.DynamicDataSource;
import org.foxfairy.base.api.core.module.MyBatisSqlConverter;
import org.foxfairy.base.api.core.module.SqlExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class BaseApiApplicationTests {

    @Resource
    DynamicDataSource dataSourceService;

    @Resource
    MyBatisSqlConverter myBatisSqlConverter;

    @Resource
    SqlExecutor sqlExecutor;

    @SneakyThrows
    @Test
    void contextLoads() {
        DynamicDataSource.DataSourceProperties dataSourcePropertiesEntity = new DynamicDataSource.DataSourceProperties();
        dataSourcePropertiesEntity.setUrl("jdbc:mysql://192.168.1.104:30764/springcloud");
        dataSourcePropertiesEntity.setPassword("84fWofwZv6");
        dataSourcePropertiesEntity.setUsername("root");
        dataSourcePropertiesEntity.setDataSourceKey("192.148.1.104");
        dataSourcePropertiesEntity.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceService.addOrUpdateDataSource(dataSourcePropertiesEntity);


        // 构造参数
        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("username", "john_doe");
        parameterMap.put("email", "john_doe@example.com");
        List<String> roles = List.of("Admin", "User");
        parameterMap.put("roles", roles);

        HashMap<String, Object> map = new HashMap<>();
        map.put("result", parameterMap);
        // 执行动态 SQL
        String dynamicSql = """
                SELECT * FROM users
                  <where> 1 = 1
                    <if test="result.username != null and result.username != ''">
                      AND username = '#{result.username}'
                    </if>
                    <if test="result.email != null and result.email != ''">
                      AND email = '#{result.email}'
                    </if>
                    <foreach collection="result.roles" item="role" separator=" OR " open=" AND (" close=") ">
                      role = '#{role}'
                    </foreach>
                  </where>
                """;


        sqlExecutor.executeQuery(dataSourcePropertiesEntity.getDataSourceKey(), dynamicSql, map);

    }

    @Test
    void useSql(){
        String dynamicSql = "SELECT * FROM users WHERE username = #{username}";
        // 构造参数
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("username", "username");

        // 使用 SqlConversion 进行 SQL 转换
//        String nativeSql = sqlConversionUtil.convertToNativeSql(dynamicSql, parameterMap);

//        System.out.println(nativeSql);
    }


    @Test
    public void testConvertToNativeSqlWithIfAndForeach() {
        // 构造参数
        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("username", "john_doe");
        parameterMap.put("email", "john_doe@example.com");
        List<String> roles = List.of("Admin", "User");
        parameterMap.put("roles", roles);

        HashMap<String, Object> map = new HashMap<>();
        map.put("result", parameterMap);
        // 执行动态 SQL
        String dynamicSql = """
                SELECT * FROM users
                  <where> 1 = 1
                    <if test="result.username != null and result.username != ''">
                      AND username = #{result.username}
                    </if>
                    <if test="result.email != null and result.email != ''">
                      AND email = #{email}
                    </if>
                    <foreach collection="result.roles" item="role" separator=" OR " open=" AND (" close=") ">
                      role = #{role}
                    </foreach>
                  </where>
                """;
        System.out.println(myBatisSqlConverter.conversionSql(dynamicSql, map));;
    }

    @Autowired
    private DynamicCodeExecutor dynamicCodeExecutor;

    @Test
    void testExecuteMethod() {
        String className = "TestClass";
        String methodName1 = "testMethod1";
        String code1 = """ 
                package test;
                public class TestClass{
                    public void testMethod1(){
                         System.out.println("hello world!!!");
                    }
                }
                """;
        String className2 = "TestClass2";
        String methodName2 = "testMethod2";
        String code2 = """ 
                package test;
                public class TestClass2{
                    public void testMethod2(){
                        new TestClass().testMethod1();
                    }
                }
                """;

        String packageName = "test";

        dynamicCodeExecutor.execute(className, methodName1, code1);
        dynamicCodeExecutor.execute(className2, methodName2, code2);

    }

}
