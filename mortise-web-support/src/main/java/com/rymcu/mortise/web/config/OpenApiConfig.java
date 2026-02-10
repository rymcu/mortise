package com.rymcu.mortise.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.web.annotation.ApiController;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;

/**
 * SpringDoc OpenAPI 配置
 * 用于生成 API 文档
 *
 * @author ronger
 */
@Slf4j
@Configuration
@ConditionalOnClass(GroupedOpenApi.class)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("SpringDoc OpenAPI 配置已加载");
        
        return new OpenAPI()
                .info(new Info()
                        .title("Mortise API 文档")
                        .version("1.0.0")
                        .description("Mortise 项目 RESTful API 接口文档")
                        .contact(new Contact()
                                .name("RYMCU")
                                .url("https://rymcu.com")
                                .email("support@rymcu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

        @Bean
        public GroupedOpenApi adminOpenApi() {
                return buildGroupedOpenApi(
                                "admin",
                                "/api/v1/admin/**",
                                AdminController.class,
                                "Admin API",
                                "Administration endpoints"
                );
        }

        @Bean
        public GroupedOpenApi publicOpenApi() {
                return GroupedOpenApi.builder()
                                .group("api")
                                .pathsToMatch("/api/v1/**")
                                .pathsToExclude("/api/v1/admin/**")
                                .addOpenApiMethodFilter(method ->
                                                method.getDeclaringClass().isAnnotationPresent(ApiController.class))
                                .addOpenApiCustomizer(openApi -> {
                                        Info info = openApi.getInfo();
                                        if (info == null) {
                                                info = new Info();
                                        }
                                        info.setTitle("Public API");
                                        info.setDescription("Public REST API endpoints");
                                        openApi.setInfo(info);
                                })
                                .build();
        }

            private GroupedOpenApi buildGroupedOpenApi(
                    String group,
                    String pathPattern,
                    Class<? extends Annotation> markerAnnotation,
                    String title,
                    String description) {
                GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                                .group(group)
                                .pathsToMatch(pathPattern)
                                .addOpenApiMethodFilter(method ->
                                                method.getDeclaringClass().isAnnotationPresent(markerAnnotation))
                                .addOpenApiCustomizer(openApi -> {
                                        Info info = openApi.getInfo();
                                        if (info == null) {
                                                info = new Info();
                                        }
                                        info.setTitle(title);
                                        info.setDescription(description);
                                        openApi.setInfo(info);
                                });

                return builder.build();
        }
}
