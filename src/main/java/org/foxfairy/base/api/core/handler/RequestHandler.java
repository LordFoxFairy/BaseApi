package org.foxfairy.base.api.core.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.common.HttpResponse;
import org.foxfairy.base.api.core.module.SqlExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 请求入口处理
 */
@Slf4j
@Component
public class RequestHandler {

    private static final String SQL_PARAM_NAME = "sql";
    private static final String DATA_SOURCE_KEY_PARAM_NAME = "key";

    @Resource
    private SqlExecutor sqlExecutor;

    /**
     * 实际请求入口
     *
     * @param request       HttpServletRequest
     * @param response      HttpServletResponse
     * @param pathVariables 路径变量
     * @param parameters    表单参数&URL参数
     * @return 返回请求结果
     */
    @ResponseBody
    public HttpResponse<Object> invoke(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable(required = false) Map<String, Object> pathVariables,
                         @RequestHeader(required = false) Map<String, Object> defaultHeaders,
                         @RequestParam(required = false) Map<String, Object> parameters) {
        
        // 获取 Cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String cookiesJson = JSON.toJSONString(cookies);
            log.info("Cookies: " + cookiesJson);
        }

        HttpSession session = request.getSession();

        // 获取 Session 属性值
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session Attribute - Name: " + attributeName + ", Value: " + attributeValue);
        }

        HashMap<String, Object> requestBody = this.getRequestBodyAsHashMap(request);

        return HttpResponse.success(this.handler(request, requestBody, parameters));
    }

    /**
     * 统一执行
     */
    @SneakyThrows
    public Object handler(HttpServletRequest request, Map<String, Object> requestBody, Map<String, Object> params){
        // 获取 sql
        String sql = this.getSQL(requestBody, params);
        String key = (String) params.get(DATA_SOURCE_KEY_PARAM_NAME);

        if(Objects.isNull(sql)){
            return null;
        }

        Map<String, Object> parameterMap = this.mergeRequestData(request, requestBody, params);

        // 怎么使用动态参数
        String result = sqlExecutor.executeQuery(key, sql, parameterMap);

        return JSON.parseArray(result);
    }


    public Map<String, Object> mergeRequestData(HttpServletRequest request, Map<String, Object> requestBody, Map<String, Object> params) {
        String requestMethod = request.getMethod();

        // 根据请求类型决定优先级
        Map<String, Object> priorityData;
        if ("POST".equals(requestMethod)) {
            priorityData = requestBody;
        } else {
            priorityData = params;
        }

        // 将优先级高的数据添加到mergedData中
        Map<String, Object> mergedData = new HashMap<>(priorityData);

        // 将优先级低的数据添加到mergedData中，如果有冲突，则保留优先级高的数据
        for (Map.Entry<String, Object> entry : (priorityData.equals(requestBody) ? params : requestBody).entrySet()) {
            if (!mergedData.containsKey(entry.getKey())) {
                mergedData.put(entry.getKey(), entry.getValue());
            }
        }

        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("result", mergedData);
        return parameterMap;
    }

    /**
     * 获取 sql
     */
    public String getSQL(Map<String, Object> requestBody, Map<String, Object> params) {
        String sql = null;
        // 从requestBody中获取SQL（虽然规范不允许，但这里作为备选方案）
        if (requestBody != null && requestBody.containsKey(SQL_PARAM_NAME)) {
            sql = (String) requestBody.get(SQL_PARAM_NAME);
        } else if (params.containsKey(SQL_PARAM_NAME)) {
            // 如果requestBody中没有SQL，则从params中获取
            sql = (String) params.get(SQL_PARAM_NAME);
        }
        return sql;
    }

    /**
     * 获取request body 字符串
     */

    private String getRequestBody(HttpServletRequest request) {
        String requestBody = null;
        // 获取请求体
        try (InputStream inputStream = request.getInputStream()) {
            requestBody = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
            return requestBody;
        } catch (IOException e) {
            throw new RuntimeException("request body获取异常", e);
        }
    }

    /**
     * 将 body 字符串转换为 map
     */

    private HashMap<String, Object> getRequestBodyAsHashMap(HttpServletRequest request) {
        String requestBody = this.getRequestBody(request);
        try {
            return JSON.parseObject(requestBody, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request body as JSON", e);
        }
    }
}
