package com.rymcu.mortise.auth.strategy;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Logto OAuth2/OIDC 提供商策略
 *
 * <p>Logto 文档：<a href="https://docs.logto.io/">...</a>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class LogtoProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER_TYPE = "logto";

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean supports(String registrationId) {
        // 支持 logto, logto-admin, logto-member 等所有 logto 开头的 registrationId
        return registrationId != null && registrationId.toLowerCase().startsWith(PROVIDER_TYPE);
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        // 创建可修改的 attributes 副本，避免 UnsupportedOperationException
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        log.debug("提取 Logto 用户信息: {}", attributes);

        // Logto 支持 OIDC，可能是 OidcUser
        if (oauth2User instanceof OidcUser oidcUser) {
            return StandardOAuth2UserInfo.builder()
                    .provider(PROVIDER_TYPE)
                    .openId(oidcUser.getSubject())                  // OIDC Subject
                    .nickname(oidcUser.getName())                   // 昵称
                    .email(oidcUser.getEmail())                     // 邮箱
                    .avatar(oidcUser.getPicture())                  // 头像
                    .emailVerified(oidcUser.getEmailVerified())     // 邮箱验证状态
                    .phone(oidcUser.getPhoneNumber())               // 手机号
                    .phoneVerified(oidcUser.getPhoneNumberVerified())
                    .rawAttributes(attributes)
                    .build();
        }

        // 降级为普通 OAuth2User
        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId(oauth2User.getName())
                .nickname((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .avatar((String) attributes.get("picture"))
                .rawAttributes(attributes)
                .build();
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
