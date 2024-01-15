package org.foxfairy.base.api.core.module.route;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.foxfairy.base.api.core.handler.RequestHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicUrlRegistry {

    @Resource
    private RequestMappingHandlerMapping mapping;
    @Resource
    private RequestHandler handler;
    private final Map<String, RequestMeta> dynamicMappings = new ConcurrentHashMap<>();

    @SneakyThrows
    public void registerDynamicUrl(String url) {
        RequestMappingInfo info = RequestMappingInfo
                    .paths(url)
                    .methods(RequestMethod.POST)
                    .build();

        Method method =  RequestHandler.class.getDeclaredMethod(
                "invoke",
                HttpServletRequest.class,
                HttpServletResponse.class,
                Map.class, Map.class, Map.class);

        // 检查是否已存在相同映射
        HandlerMethod handlerMethod = mapping.getHandlerMethods().get(info);
        if (Objects.isNull(handlerMethod)) {
            // 不存在，进行注册
            mapping.registerMapping(info, handler, method);
        }
        // 更新
        dynamicMappings.put(url, new RequestMeta(url, info));
    }

    @SneakyThrows
    public void reset() {
        // 清除之前设置的所有动态路由
        dynamicMappings.keySet().forEach(url -> mapping.unregisterMapping(dynamicMappings.get(url).getInfo()));

        // 重新加载新的动态路由
        // 这里可以根据实际需求重新加载新的路由信息
        for (String url : dynamicMappings.keySet()) {
            this.registerDynamicUrl(url);
        }

        Thread.sleep(0);
    }

    public void removeDynamicUrl(String url){
        if (Objects.nonNull(dynamicMappings.get(url) )) {
            mapping.unregisterMapping(dynamicMappings.get(url).getInfo());
            dynamicMappings.remove(url);
        }
    }

    @Data
    @AllArgsConstructor
    public static class RequestMeta{
        private String url;
        private RequestMappingInfo info;
    }
}

