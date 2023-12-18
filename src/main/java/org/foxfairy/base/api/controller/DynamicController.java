package org.foxfairy.base.api.controller;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.foxfairy.base.api.core.common.HttpResponse;
import org.foxfairy.base.api.core.module.DynamicDataSource;
import org.foxfairy.base.api.core.module.DynamicUrlRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DynamicController {

    @Resource
    private DynamicUrlRegistry dynamicUrlService;
    @Resource
    DynamicDataSource dataSourceService;

    @GetMapping("/register")
    public HttpResponse<Object> registerDynamicUrl(@Nonnull @Param("url") String url) {
        // 例如，注册动态 URL "/dynamic" 对应的方法为 "handleDynamicRequest"
        dynamicUrlService.registerDynamicUrl(url);
        addRoute();
        return HttpResponse.success("Dynamic URL register successfully!");
    }

    @GetMapping("/remove")
    public HttpResponse<Object> removeDynamicUrl(@Nonnull @Param("url") String url) {
        dynamicUrlService.removeDynamicUrl(url);
        return HttpResponse.success("Dynamic URL removed successfully!");
    }

    public HttpResponse<DynamicDataSource.DataSourceMeta> addRoute(){
        DynamicDataSource.DataSourceProperties dataSourcePropertiesEntity = new DynamicDataSource.DataSourceProperties();
        dataSourcePropertiesEntity.setUrl("jdbc:mysql://192.168.1.104:30764/springcloud");
        dataSourcePropertiesEntity.setPassword("84fWofwZv6");
        dataSourcePropertiesEntity.setUsername("root");
        dataSourcePropertiesEntity.setDataSourceKey("192.148.1.104");
        dataSourcePropertiesEntity.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceService.addOrUpdateDataSource(dataSourcePropertiesEntity);
        return HttpResponse.success(DynamicDataSource.DATA_SOURCE_META.get(dataSourcePropertiesEntity.getDataSourceKey()));
    }
}

