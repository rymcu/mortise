package com.rymcu.mortise.auth.spi;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 安全配置扩展接口 (SPI)
 * 业务模块可实现此接口来自定义安全配置
 * 
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class CustomSecurityConfigurer implements SecurityConfigurer {
 *     &#64;Override
 *     public void configureAuthorization(
 *         AuthorizeHttpRequestsConfigurer&lt;HttpSecurity&gt;.AuthorizationManagerRequestMatcherRegistry registry
 *     ) {
 *         registry.requestMatchers("/api/custom/**").hasRole("ADMIN");
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface SecurityConfigurer {

    /**
     * 获取优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 配置授权规则
     * <p>
     * 注意：此方法在 anyRequest() 之前调用，确保自定义规则优先生效
     * 
     * @param registry 授权请求匹配器注册表
     */
    void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    );

    /**
     * 是否启用该配置
     * 默认启用
     */
    default boolean isEnabled() {
        return true;
    }
}
