package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.auth.service.SilentAuthService;
import com.rymcu.mortise.wechat.service.WeChatLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信静默授权服务实现
 * <p>
 * 实现 auth 模块定义的 {@link SilentAuthService} SPI 接口，
 * 委托给 {@link WeChatLoginService} 完成实际操作。
 * <p>
 * 关键区别：此流程完全绕开 Spring Security OAuth2 登录，
 * 直接调用微信 API，仅获取 openid，不会触发登录/绑定操作。
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatSilentAuthServiceImpl implements SilentAuthService {

    private final WeChatLoginService weChatLoginService;

    @Override
    public String buildSilentAuthUrl(String appId, String redirectUri, String state) {
        return weChatLoginService.buildSilentAuthUrl(appId, redirectUri, state);
    }

    @Override
    public String getOpenIdByCode(String appId, String code) {
        return weChatLoginService.getOpenIdByCode(appId, code);
    }
}
