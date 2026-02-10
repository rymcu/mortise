package com.rymcu.mortise.web.config;

import lombok.extern.slf4j.Slf4j;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.web.annotation.ApiController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    /**
     * 配置 CORS 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        log.info("CORS 跨域配置已加载");
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);

        // API 文档
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        log.info("静态资源处理配置已加载");
    }

    /**
     * 统一接口前缀：
     * - 使用 @AdminController 统一加 /api/v1/admin
     * - 使用 @ApiController 统一加 /api/v1
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1/admin", HandlerTypePredicate.forAnnotation(AdminController.class));
        configurer.addPathPrefix("/api/v1", HandlerTypePredicate.forAnnotation(ApiController.class));

        log.info("接口前缀配置已加载: /api/v1/admin/**, /api/v1/**");
    }
}
