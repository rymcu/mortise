package com.rymcu.mortise.auth.strategy;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信 OAuth2 提供商策略
 *
 * <p>微信开放平台文档：<a href="https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html">链接</a>
 * <p>注意：微信 OAuth2 与标准协议有差异，需要特殊处理
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class WeChatProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER_TYPE = "wechat";

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean supports(String registrationId) {
        // 支持 wechat 和 wechat-open（开放平台）
        return registrationId != null && registrationId.toLowerCase().startsWith("wechat");
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        log.debug("提取微信用户信息: {}", attributes);

        // 微信返回的性别字段：0-未知，1-男，2-女
        Integer sex = (Integer) attributes.get("sex");

        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId((String) attributes.get("openid"))        // 微信 OpenID
                .unionId((String) attributes.get("unionid"))      // 微信 UnionID（开放平台）
                .nickname((String) attributes.get("nickname"))    // 昵称
                .avatar((String) attributes.get("headimgurl"))    // 头像 URL
                .gender(sex)                                      // 性别
                .country((String) attributes.get("country"))      // 国家
                .province((String) attributes.get("province"))    // 省份
                .city((String) attributes.get("city"))            // 城市
                .language((String) attributes.get("language"))    // 语言
                .rawAttributes(attributes)
                .build();
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
