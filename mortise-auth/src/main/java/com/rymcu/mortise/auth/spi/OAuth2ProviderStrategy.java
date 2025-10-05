package com.rymcu.mortise.auth.spi;

import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 提供商策略接口
 * <p>
 * 用于扩展不同的 OAuth2 提供商（GitHub、Google、微信等）
 * 每个提供商可以实现自己的用户信息提取和标准化逻辑
 *
 * @author ronger
 * @since 1.0.0
 */
public interface OAuth2ProviderStrategy {

    /**
     * 获取提供商类型
     * 
     * @return 提供商标识，如 "github", "google", "wechat", "logto"
     */
    String getProviderType();

    /**
     * 是否支持该提供商
     * 
     * @param registrationId Spring Security OAuth2 客户端注册 ID
     * @return 是否支持
     */
    boolean supports(String registrationId);

    /**
     * 提取标准化的用户信息
     * 
     * @param oauth2User Spring Security OAuth2 用户对象
     * @return 标准化的用户数据
     */
    StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User);

    /**
     * 获取优先级（数字越小优先级越高）
     * 用于多个策略都支持同一个 registrationId 时的选择
     * 
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 是否启用该策略
     * 
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}
