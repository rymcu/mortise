package com.rymcu.mortise.auth.strategy;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Google OAuth2 提供商策略
 * 
 * <p>Google API 文档：https://developers.google.com/identity/protocols/oauth2
 * 
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class GoogleProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER_TYPE = "google";

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean supports(String registrationId) {
        return PROVIDER_TYPE.equalsIgnoreCase(registrationId);
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.debug("提取 Google 用户信息: {}", attributes);

        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId((String) attributes.get("sub"))           // Google Subject ID
                .nickname((String) attributes.get("name"))        // 用户名
                .email((String) attributes.get("email"))          // 邮箱
                .avatar((String) attributes.get("picture"))       // 头像 URL
                .emailVerified((Boolean) attributes.get("email_verified"))
                .language((String) attributes.get("locale"))      // 语言偏好
                .rawAttributes(attributes)
                .build();
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
