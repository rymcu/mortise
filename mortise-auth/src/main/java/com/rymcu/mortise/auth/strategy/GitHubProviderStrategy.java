package com.rymcu.mortise.auth.strategy;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * GitHub OAuth2 提供商策略
 *
 * <p>GitHub API 文档：<a href="https://docs.github.com/en/rest/users/users">...</a>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class GitHubProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER_TYPE = "github";

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
        // 创建可修改的 attributes 副本，避免 UnsupportedOperationException
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        log.debug("提取 GitHub 用户信息: {}", attributes);

        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId(String.valueOf(attributes.get("id")))  // GitHub 使用数字 ID
                .nickname((String) attributes.get("login"))    // GitHub 用户名
                .realName((String) attributes.get("name"))     // 真实姓名（可能为空）
                .email((String) attributes.get("email"))       // 邮箱（可能为空或隐私保护）
                .avatar((String) attributes.get("avatar_url")) // 头像 URL
                .rawAttributes(attributes)
                .build();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
