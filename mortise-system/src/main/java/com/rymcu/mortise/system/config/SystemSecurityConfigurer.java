package com.rymcu.mortise.system.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 系统模块的安全配置扩展
 * <p>
 * 通过 SecurityConfigurer SPI 配置 Auth 端点的访问权限
 * <strong>无需依赖 mortise-auth 模块</strong>（仅依赖 SPI 接口）
 * <p>
 * <strong>架构设计原则</strong>:
 * - system 和 auth 是同一层级的模块，不应该有依赖关系
 * - 使用 SPI 扩展机制实现解耦
 * - 系统模块独立管理自己的安全策略
 *
 * @author ronger
 */
@Slf4j
@Component
public class SystemSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 200; // 低优先级
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        // 认证相关 API - 无需认证
        registry.requestMatchers("/api/v1/auth/login").permitAll();
        registry.requestMatchers("/api/v1/auth/register").permitAll();
        registry.requestMatchers("/api/v1/auth/logout").permitAll();
        registry.requestMatchers("/api/v1/auth/refresh-token").permitAll();
        registry.requestMatchers("/api/v1/auth/password/request").permitAll();
        registry.requestMatchers("/api/v1/auth/password/reset").permitAll();
        registry.requestMatchers("/api/v1/auth/email/request").permitAll();

        log.info("系统模块安全配置已加载: Auth 端点放行");
    }

    @Override
    public boolean isEnabled() {
        // 监控端点配置始终启用
        return true;
    }
}
