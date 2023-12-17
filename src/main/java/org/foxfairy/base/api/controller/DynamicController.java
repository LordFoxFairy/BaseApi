package org.foxfairy.base.api.controller;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.foxfairy.base.api.core.common.HttpResponse;
import org.foxfairy.base.api.core.route.DynamicUrlRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DynamicController {

    @Resource
    private DynamicUrlRegistry dynamicUrlService;

    @GetMapping("/register")
    public HttpResponse<Object> registerDynamicUrl(@Nonnull @Param("url") String url) throws NoSuchMethodException {
        // 例如，注册动态 URL "/dynamic" 对应的方法为 "handleDynamicRequest"
        dynamicUrlService.registerDynamicUrl(url);
        return HttpResponse.success("Dynamic URL register successfully!");
    }

    @GetMapping("/remove")
    public HttpResponse<Object> removeDynamicUrl(@Nonnull @Param("url") String url) throws NoSuchMethodException {
        dynamicUrlService.removeDynamicUrl(url);
        return HttpResponse.success("Dynamic URL removed successfully!");
    }
}

