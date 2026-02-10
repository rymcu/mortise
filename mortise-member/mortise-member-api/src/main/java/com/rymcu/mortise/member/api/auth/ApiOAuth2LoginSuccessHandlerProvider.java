package com.rymcu.mortise.member.api.auth;

import com.rymcu.mortise.auth.spi.OAuth2LoginSuccessHandlerProvider;
import com.rymcu.mortise.member.api.handler.ApiOAuth2LoginSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * API 客户端 OAuth2 登录成功处理器提供者
 * <p>
 * 通过 SPI 机制注册 API 客户端的 OAuth2 登录处理器
 * <p>
 * 支持的 registrationId:
 * - wechat-app: 微信 APP 登录
 * - github-app: GitHub APP 登录
 * - google-app: Google APP 登录
 * - logto-app: Logto APP 登录
 * <p>
 * 使用 ObjectProvider 延迟加载，避免循环依赖
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class ApiOAuth2LoginSuccessHandlerProvider implements OAuth2LoginSuccessHandlerProvider {

    private final ObjectProvider<ApiOAuth2LoginSuccessHandler> handlerProvider;

    /**
     * 构造函数注入 ObjectProvider（延迟加载）
     *
     * @param handlerProvider ApiOAuth2LoginSuccessHandler 的提供者
     */
    public ApiOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<ApiOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    public AuthenticationSuccessHandler getHandler() {
        // 延迟获取 Handler 实例，避免循环依赖
        return handlerProvider.getObject();
    }

    @Override
    public String[] getSupportedRegistrationIds() {
        // API 客户端支持的 OAuth2 客户端注册 ID
        return new String[] {
            "wechat-app",   // 微信 APP 登录
            "github-app",   // GitHub APP 登录
            "google-app",   // Google APP 登录
            "logto-app"     // Logto APP 登录
        };
    }

    @Override
    public boolean isDefault() {
        // API 客户端不作为默认处理器
        return false;
    }

    @Override
    public int getOrder() {
        // 中等优先级（50）
        return 50;
    }

    @Override
    public boolean isEnabled() {
        // 始终启用
        return true;
    }
}
