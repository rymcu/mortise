package com.rymcu.mortise.system.auth;

import com.rymcu.mortise.auth.spi.OAuth2LoginSuccessHandlerProvider;
import com.rymcu.mortise.system.handler.SystemOAuth2LoginSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 系统管理端 OAuth2 登录成功处理器提供者
 * <p>
 * 通过 SPI 机制注册系统管理端的 OAuth2 登录处理器
 * <p>
 * 支持的 registrationId:
 * - logto: 默认的 Logto 配置
 * - logto-admin: 管理后台专用的 Logto 配置
 * <p>
 * 使用 ObjectProvider 延迟加载，避免循环依赖
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class SystemOAuth2LoginSuccessHandlerProvider implements OAuth2LoginSuccessHandlerProvider {

    private final ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider;

    /**
     * 构造函数注入 ObjectProvider（延迟加载）
     *
     * @param handlerProvider SystemOAuth2LoginSuccessHandler 的提供者
     */
    public SystemOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    public AuthenticationSuccessHandler getHandler() {
        // 延迟获取 Handler 实例，避免循环依赖
        return handlerProvider.getObject();
    }

    @Override
    public String[] getSupportedRegistrationIds() {
        // 系统管理端支持的 OAuth2 客户端注册 ID
        return new String[] {
            "logto",        // 默认 Logto 配置（向后兼容）
            "logto-admin"   // 管理后台专用 Logto 配置
        };
    }

    @Override
    public boolean isDefault() {
        // 系统管理端作为默认处理器（向后兼容）
        return true;
    }

    @Override
    public int getOrder() {
        // 高优先级（100）
        return 100;
    }

    @Override
    public boolean isEnabled() {
        // 始终启用
        return true;
    }
}
