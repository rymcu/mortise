package com.rymcu.mortise.member.api.listener;

import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.event.QRCodeScanEvent;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.auth.util.OAuth2ProviderUtils;
import com.rymcu.mortise.member.api.handler.ApiOAuth2LoginSuccessHandler;
import com.rymcu.mortise.member.api.model.OAuth2LoginResponse;
import com.rymcu.mortise.member.api.service.OAuth2MemberBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 二维码扫描事件监听器
 * <p>
 * 监听微信扫码事件，处理登录业务逻辑：
 * 1. 查找用户绑定关系
 * 2. 生成完整的登录响应（包含 Token 和用户信息）
 * 3. 更新缓存状态
 * <p>
 * 设计参考：{@link ApiOAuth2LoginSuccessHandler}
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QRCodeScanEventListener {

    private final AuthCacheService authCacheService;
    private final OAuth2MemberBindingService oauth2MemberBindingService;
    private final Oauth2ClientConfigService oauth2ClientConfigService;

    /**
     * 处理二维码扫描事件
     * <p>
     * 异步处理，避免阻塞微信消息响应
     */
    @Async
    @EventListener
    public void handleQRCodeScanEvent(QRCodeScanEvent event) {
        String sceneStr = event.getSceneStr();
        String openId = event.getUserId();
        String clientId = event.getClientId();
        String eventType = event.getEventType();

        log.info("收到二维码扫描事件 - sceneStr: {}, openId: {}, clientId: {}, eventType: {}",
                sceneStr, openId, clientId, eventType);

        // 参数校验
        if (sceneStr == null || sceneStr.trim().isEmpty()) {
            log.warn("场景值为空，忽略事件");
            return;
        }
        if (openId == null || openId.trim().isEmpty()) {
            log.warn("OpenId 为空，忽略事件");
            return;
        }

        // 检查二维码是否存在且有效
        Integer currentState = authCacheService.getOAuth2QrcodeState(sceneStr);
        if (currentState == null) {
            log.warn("二维码不存在或已过期 - sceneStr: {}", sceneStr);
            return;
        }

        try {
            // 1. 更新二维码状态为"已扫描"
            authCacheService.storeOAuth2QrcodeState(sceneStr, QrcodeState.SCANNED.getValue());

            // 2. 从事件中获取用户信息并构造 OAuth2User 对象
            OAuth2User oauth2User = buildOAuth2UserFromEvent(event);

            Oauth2ClientConfig clientConfig = oauth2ClientConfigService.loadOauth2ClientConfigByClientId(clientId);
            // 3. 确定提供商类型和 registrationId
            String providerType = OAuth2ProviderUtils.determineProviderType(clientConfig.getRegistrationId());

            // 4. 调用 bindOrLoginWithOAuth2User 自动绑定或创建新用户
            OAuth2LoginResponse loginResponse = oauth2MemberBindingService.bindOrLoginWithOAuth2User(
                    oauth2User, providerType, clientConfig.getRegistrationId());

            // 5. 存储完整的登录响应到缓存（而不只是 token）
            authCacheService.storeOAuth2LoginResponse(sceneStr, loginResponse);

            // 6. 更新状态为"已授权"（与 ApiOAuth2LoginSuccessHandler 保持一致）
            authCacheService.storeOAuth2QrcodeState(sceneStr, QrcodeState.AUTHORIZED.getValue());

            log.info("微信扫码登录成功 - sceneStr: {}, openId: {}, memberId: {}, username: {}",
                    sceneStr, openId, loginResponse.memberId(), loginResponse.username());

        } catch (Exception e) {
            log.error("处理微信扫码事件失败 - sceneStr: {}, openId: {}", sceneStr, openId, e);
            // 更新状态为"已取消"
            authCacheService.storeOAuth2QrcodeState(sceneStr, QrcodeState.CANCELED.getValue());
        }
    }

    /**
     * 从事件中构造 OAuth2User 对象
     * <p>
     * 只使用 openId 构建最基本的 OAuth2User，让 bindOrLoginWithOAuth2User 自动处理用户创建
     *
     * @param event 扫码事件
     * @return OAuth2User 对象
     */
    private OAuth2User buildOAuth2UserFromEvent(QRCodeScanEvent event) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("openid", event.getUserId());

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "openid"
        );
    }

}
