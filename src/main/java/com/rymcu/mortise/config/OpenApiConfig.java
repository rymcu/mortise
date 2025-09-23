package com.rymcu.mortise.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置
 *
 * @author ronger
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mortiseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mortise API")
                        .description("一款现代化的后台管理脚手架项目 API 文档")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("RYMCU")
                                .email("ronger-x@outlook.com")
                                .url("https://rymcu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}
