package com.rymcu.mortise.wechat.integration;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.wechat.service.WeChatLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信 OAuth2 适配器
 * <p>将微信登录集成到现有的 OAuth2 认证体系中</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(WeChatLoginService.class)
public class WeChatOAuth2Adapter implements OAuth2ProviderStrategy {

    private static final String PROVIDER_TYPE = "wechat-qrcode";
    
    private final WeChatLoginService weChatLoginService;

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean supports(String registrationId) {
        return registrationId != null && 
               (registrationId.equals("wechat-qrcode") || 
                registrationId.equals("wechat-h5"));
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.debug("提取微信扫码登录用户信息: {}", attributes);

        Integer sex = (Integer) attributes.get("sex");

        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId((String) attributes.get("openid"))
                .unionId((String) attributes.get("unionid"))
                .nickname((String) attributes.get("nickname"))
                .avatar((String) attributes.get("headimgurl"))
                .gender(sex)
                .country((String) attributes.get("country"))
                .province((String) attributes.get("province"))
                .city((String) attributes.get("city"))
                .language((String) attributes.get("language"))
                .rawAttributes(attributes)
                .build();
    }

    /**
     * 直接通过授权码获取用户信息（使用默认账号）
     *
     * @param code 授权码
     * @return 标准化的用户信息
     * @throws WxErrorException 微信API异常
     */
    public StandardOAuth2UserInfo getUserInfoByCode(String code) throws WxErrorException {
        return getUserInfoByCode(null, code);
    }

    /**
     * 直接通过授权码获取用户信息（指定账号）
     *
     * @param accountId 账号ID（null 表示使用默认公众号账号）
     * @param code      授权码
     * @return 标准化的用户信息
     * @throws WxErrorException 微信API异常
     */
    public StandardOAuth2UserInfo getUserInfoByCode(Long accountId, String code) throws WxErrorException {
        WxOAuth2UserInfo wxUserInfo = weChatLoginService.getUserInfoByCode(accountId, code);
        
        Map<String, Object> attributes = convertToMap(wxUserInfo);
        
        return StandardOAuth2UserInfo.builder()
                .provider(PROVIDER_TYPE)
                .openId(wxUserInfo.getOpenid())
                .unionId(wxUserInfo.getUnionId())
                .nickname(wxUserInfo.getNickname())
                .avatar(wxUserInfo.getHeadImgUrl())
                .gender(wxUserInfo.getSex())
                .country(wxUserInfo.getCountry())
                .province(wxUserInfo.getProvince())
                .city(wxUserInfo.getCity())
                .language("zh_CN") // 默认中文
                .rawAttributes(attributes)
                .build();
    }

    @Override
    public int getOrder() {
        return 35;
    }

    /**
     * 将微信用户信息转换为 Map
     */
    private Map<String, Object> convertToMap(WxOAuth2UserInfo wxUserInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("openid", wxUserInfo.getOpenid());
        map.put("unionid", wxUserInfo.getUnionId());
        map.put("nickname", wxUserInfo.getNickname());
        map.put("headimgurl", wxUserInfo.getHeadImgUrl());
        map.put("sex", wxUserInfo.getSex());
        map.put("country", wxUserInfo.getCountry());
        map.put("province", wxUserInfo.getProvince());
        map.put("city", wxUserInfo.getCity());
        map.put("language", "zh_CN"); // 默认中文
        map.put("privilege", new java.util.ArrayList<>()); // 空列表
        return map;
    }
}
