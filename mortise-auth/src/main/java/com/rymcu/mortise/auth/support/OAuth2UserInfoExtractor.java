package com.rymcu.mortise.auth.support;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 用户信息提取器
 * <p>
 * 根据不同的 OAuth2 提供商，自动选择对应的策略提取用户信息
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
public class OAuth2UserInfoExtractor {

    private final List<OAuth2ProviderStrategy> strategies;

    @Autowired
    public OAuth2UserInfoExtractor(Optional<List<OAuth2ProviderStrategy>> strategiesOptional) {
        this.strategies = strategiesOptional.orElse(List.of());
        log.info("OAuth2UserInfoExtractor 初始化，发现 {} 个提供商策略", this.strategies.size());

        this.strategies.forEach(strategy ->
            log.info("  - {} (优先级: {})", strategy.getProviderType(), strategy.getOrder())
        );
    }

    /**
     * 提取标准化的用户信息
     *
     * @param oauth2User Spring Security OAuth2 用户对象
     * @param registrationId 客户端注册 ID
     * @return 标准化的用户信息
     */
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User, String registrationId) {
        log.debug("开始提取用户信息: registrationId={}", registrationId);

        // 查找支持该 registrationId 的策略
        OAuth2ProviderStrategy strategy = strategies.stream()
                .filter(OAuth2ProviderStrategy::isEnabled)
                .filter(s -> s.supports(registrationId))
                .min(Comparator.comparingInt(OAuth2ProviderStrategy::getOrder))
                .orElse(null);

        if (strategy == null) {
            log.warn("未找到支持 registrationId={} 的 OAuth2 提供商策略，使用默认提取逻辑", registrationId);
            return extractDefaultUserInfo(oauth2User, registrationId);
        }

        log.info("使用 {} 策略提取用户信息 (registrationId={})", strategy.getProviderType(), registrationId);
        StandardOAuth2UserInfo userInfo = strategy.extractUserInfo(oauth2User);

        // 确保 provider 字段已设置（使用 registrationId 以区分不同配置）
        if (userInfo.getProvider() == null) {
            userInfo.setProvider(registrationId);
        }

        // 保存原始的 registrationId 到 rawAttributes
        // 注意: rawAttributes 可能是不可修改的 Map，需要创建新的可修改 Map
        if (userInfo.getRawAttributes() != null) {
            Map<String, Object> mutableAttributes = new HashMap<>(userInfo.getRawAttributes());
            mutableAttributes.put("_registrationId", registrationId);
            userInfo.setRawAttributes(mutableAttributes);
        }

        log.debug("用户信息提取完成: provider={}, openId={}, nickname={}",
            userInfo.getProvider(), userInfo.getOpenId(), userInfo.getNickname());
        return userInfo;
    }

    /**
     * 默认的用户信息提取逻辑
     * 当没有找到匹配的策略时使用
     */
    private StandardOAuth2UserInfo extractDefaultUserInfo(OAuth2User oauth2User, String registrationId) {
        return StandardOAuth2UserInfo.builder()
                .provider(registrationId)
                .openId(oauth2User.getName())
                .nickname(oauth2User.getAttribute("name"))
                .email(oauth2User.getAttribute("email"))
                .avatar(oauth2User.getAttribute("picture"))
                .rawAttributes(oauth2User.getAttributes())
                .build();
    }
}
