package com.rymcu.mortise.member.api.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 商城模块安全配置
 * <p>
 * 通过 SPI 扩展机制向主安全配置添加商城相关的授权规则
 * <p>
 * 路径规则:
 * - /api/v1/app/auth/** : 公开接口 (注册、登录、发送验证码等)
 * - /api/v1/app/** : 需要会员认证 (ROLE_MEMBER)
 *
 * @author ronger
 */
@Slf4j
@Component
public class AppSecurityConfigurer implements SecurityConfigurer {

    /**
     * 配置授权规则
     * <p>
     * 优先级设置为 50，在默认规则 (100) 之前执行
     */
    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        log.info("应用商城模块安全配置...");

        registry
                // 公开的认证接口 (注册、登录、验证码、刷新Token)
                .requestMatchers(
                        "/api/v1/app/common/**",
                        "/api/v1/app/auth/register",
                        "/api/v1/app/auth/login",
                        "/api/v1/app/auth/login-by-phone",
                        "/api/v1/app/auth/refresh-token",
                        "/api/v1/app/auth/refresh-token-by-jwt",
                        "/api/v1/app/auth/send-code",
                        "/api/v1/app/auth/verify-code"
                ).permitAll()
                // OAuth2 认证相关接口 (二维码、授权URL、状态查询、回调)
                .requestMatchers("/api/v1/app/oauth2/**").permitAll()
                // 其他商城接口需要会员角色
                .requestMatchers("/api/v1/app/**").hasRole("MEMBER");

        log.info("商城安全配置完成: /api/v1/app/auth/*, /api/v1/app/oauth2/** (公开), /api/v1/app/** (需要 ROLE_MEMBER)");
    }

    /**
     * 设置优先级为 50
     * 确保商城规则在 WebSecurityConfig 的 anyRequest().authenticated() 之前应用
     */
    @Override
    public int getOrder() {
        return 50;
    }
}
