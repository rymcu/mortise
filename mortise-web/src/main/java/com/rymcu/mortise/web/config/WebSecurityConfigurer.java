package com.rymcu.mortise.web.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * Web 模块安全配置
 * 
 * <p>通过 SPI 扩展机制为 OpenAPI/Swagger 和静态资源配置公开访问权限</p>
 * 
 * <p><strong>架构说明：</strong></p>
 * <ul>
 *     <li>本配置类位于 mortise-web 模块，负责 Web 层相关的安全策略</li>
 *     <li>不直接依赖 mortise-auth 模块（通过 optional 依赖使用 SPI 接口）</li>
 *     <li>遵循"各模块管理自己的安全策略"原则</li>
 * </ul>
 * 
 * <p><strong>放行端点：</strong></p>
 * <ul>
 *     <li>OpenAPI 文档: /v3/api-docs/**, /swagger-ui/**</li>
 *     <li>静态资源: /static/**, /webjars/**, /favicon.ico</li>
 *     <li>认证端点: /api/v1/auth/login, /register, /logout</li>
 *     <li>CORS 预检: OPTIONS 请求</li>
 * </ul>
 * 
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class WebSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        // 中等优先级（100）
        // monitor=50 (高优先级), web=100 (中优先级), system=200 (低优先级)
        return 100;
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        
        authorize
            // OpenAPI 3.0 文档端点
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/swagger-resources/**").permitAll()
            .requestMatchers("/webjars/springdoc-openapi-ui/**").permitAll()
            
            // 静态资源
            .requestMatchers("/static/**").permitAll()
            .requestMatchers("/webjars/**").permitAll()
            .requestMatchers("/favicon.ico").permitAll()
            .requestMatchers("/css/**").permitAll()
            .requestMatchers("/js/**").permitAll()
            .requestMatchers("/images/**").permitAll()
            
            // 认证相关公开端点
            .requestMatchers("/api/v1/auth/login").permitAll()
            .requestMatchers("/api/v1/auth/register").permitAll()
            .requestMatchers("/api/v1/auth/logout").permitAll()
            .requestMatchers("/api/v1/auth/refresh-token").permitAll()
            .requestMatchers("/api/v1/auth/forgot-password").permitAll()
            
            // 公开 API 端点（根据业务需求添加）
            .requestMatchers("/api/v1/public/**").permitAll()
            
            // CORS 预检请求
            .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll();
        
        log.info("Web 模块安全配置已加载: OpenAPI 文档、静态资源、认证端点放行");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
