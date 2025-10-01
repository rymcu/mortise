package com.rymcu.mortise.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 * 用于生成 API 文档
 *
 * @author ronger
 */
@Slf4j
@Configuration
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
}
