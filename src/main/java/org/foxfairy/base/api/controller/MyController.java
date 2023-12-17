package org.foxfairy.base.api.controller;
import org.foxfairy.base.api.core.route.DynamicUrlRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MyController {

    @Autowired
    private DynamicUrlRegistry dynamicUrlService;

    @GetMapping("/register")
    public String registerDynamicUrl() throws NoSuchMethodException {
        // 例如，注册动态 URL "/dynamic" 对应的方法为 "handleDynamicRequest"
        dynamicUrlService.registerDynamicUrl("/dynamic");
        return "Dynamic URL registered successfully!";
    }

    @GetMapping("/remove")
    public String removeDynamicUrl() throws NoSuchMethodException {
        dynamicUrlService.removeDynamicUrl("/dynamic");
        return "Dynamic URL removed successfully!";
    }
}

