package org.foxfairy.base.api.core.route;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.foxfairy.base.api.core.common.HttpResponse;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求入口处理
 */
@Slf4j
@Component
public class RequestHandler {

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

        String requestBody = this.getRequestBody(request);

        String result = "香菜";
        HttpResponse<Object> content = HttpResponse.success(result);

        return content;
    }

    /**
     * 获取request body
     * @param request
     * @return
     */
    private String getRequestBody(HttpServletRequest request) {
        String requestBody = null;
        // 获取请求体
        try (InputStream inputStream = request.getInputStream()) {
            requestBody = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
            log.info("Request Body: " + requestBody);

            return requestBody;
        } catch (IOException e) {
            // 处理 IO 异常E
            log.error("request body获取异常：{}", e.getMessage());
        }
        return requestBody;
    }
}
