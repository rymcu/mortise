package com.rymcu.mortise.wechat.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * Created on 2025/10/6 19:18.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.wechat.config
 */
@Slf4j
@Component
public class WeChatSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 200; // 低优先级
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        // 微信公众号回调 API - 无需认证
        registry.requestMatchers("/api/v1/wechat/portal/**").permitAll();
        // 认证相关 API - 无需认证
        registry.requestMatchers("/api/v1/wechat/auth/**").permitAll();

        log.info("WeChat 模块安全配置已加载: Auth 端点放行");
    }

    @Override
    public boolean isEnabled() {
        // 监控端点配置始终启用
        return true;
    }
}
