package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.service.DynamicWeChatOpenServiceManager;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import com.rymcu.mortise.wechat.service.WeChatLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.open.api.WxOpenService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 微信登录服务实现类
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatLoginServiceImpl implements WeChatLoginService {

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;
    private final DynamicWeChatOpenServiceManager dynamicWeChatOpenServiceManager;

    @Override
    public String buildAuthorizationUrl(Long accountId, String redirectUri, String state) {
        log.info("构建微信扫码登录授权 URL - accountId: {}, redirectUri: {}", accountId, redirectUri);
        String appId = dynamicWeChatServiceManager.getAppIdByAccountId(accountId);
        try {
            WxOpenService wxOpenService = dynamicWeChatOpenServiceManager.getService();
            if (wxOpenService == null) {
                throw new RuntimeException("未找到可用的微信公众号服务");
            }

            // snsapi_login 授权作用域，获取用户详细信息
            String authUrl = wxOpenService.getWxOpenComponentService()
                    .getPreAuthUrl(redirectUri, WxConsts.QrConnectScope.SNSAPI_LOGIN, appId);

            log.info("生成授权 URL 成功: {}", maskUrl(authUrl));
            return authUrl;

        } catch (Exception e) {
            log.error("构建授权 URL 失败", e);
            throw new RuntimeException("构建微信授权链接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildH5AuthorizationUrl(Long accountId, String redirectUri, String state) {
        log.info("构建微信 H5 授权 URL - accountId: {}, redirectUri: {}", accountId, redirectUri);

        try {
            WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAccountId(accountId);
            if (wxMpService == null) {
                throw new RuntimeException("未找到可用的微信公众号服务");
            }

            // snsapi_base 授权作用域，静默授权，只获取 openId
            String authUrl = wxMpService.getOAuth2Service()
                    .buildAuthorizationUrl(redirectUri, WxConsts.OAuth2Scope.SNSAPI_BASE, state);

            log.info("生成 H5 授权 URL 成功: {}", maskUrl(authUrl));
            return authUrl;

        } catch (Exception e) {
            log.error("构建 H5 授权 URL 失败", e);
            throw new RuntimeException("构建微信 H5 授权链接失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "wechat:login", key = "'userInfo:' + #code", unless = "#result == null")
    public WxOAuth2UserInfo getUserInfoByCode(Long accountId, String code) throws WxErrorException {
        log.info("通过授权码获取用户信息 - accountId: {}, code: {}", accountId, maskString(code));

        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAccountId(accountId);
        if (wxMpService == null) {
            throw new RuntimeException("未找到可用的微信公众号服务");
        }

        // 1. 通过 code 获取 access_token
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        log.info("获取 access_token 成功 - openId: {}", accessToken.getOpenId());

        // 2. 通过 access_token 获取用户信息
        WxOAuth2UserInfo userInfo = wxMpService.getOAuth2Service()
                .getUserInfo(accessToken, null);

        log.info("获取用户信息成功 - openId: {}, nickname: {}",
                userInfo.getOpenid(), userInfo.getNickname());

        return userInfo;
    }

    @Override
    public boolean validateAccessToken(String accessToken, String openId) {
        log.info("验证 access_token - openId: {}", openId);

        try {
            WxMpService wxMpService = dynamicWeChatServiceManager.getDefaultService();
            if (wxMpService == null) {
                log.error("未找到可用的微信公众号服务");
                return false;
            }

            WxOAuth2AccessToken token = new WxOAuth2AccessToken();
            token.setAccessToken(accessToken);
            token.setOpenId(openId);

            boolean valid = wxMpService.getOAuth2Service().validateAccessToken(token);
            log.info("access_token 验证结果: {}", valid);

            return valid;

        } catch (Exception e) {
            log.error("验证 access_token 失败", e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = "wechat:login", allEntries = true)
    public void refreshCache() {
        log.info("刷新微信登录缓存");
    }

    /**
     * 脱敏字符串（用于日志）
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 8) {
            return "***";
        }
        return str.substring(0, 4) + "***" + str.substring(str.length() - 4);
    }

    /**
     * 脱敏 URL（用于日志）
     */
    private String maskUrl(String url) {
        if (url == null) {
            return null;
        }
        // 只显示协议和域名部分
        int endIndex = url.indexOf('?');
        return endIndex > 0 ? url.substring(0, endIndex) + "?..." : url;
    }
}
