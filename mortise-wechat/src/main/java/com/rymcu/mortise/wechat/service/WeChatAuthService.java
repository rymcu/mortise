package com.rymcu.mortise.wechat.service;

import com.rymcu.mortise.wechat.integration.WeChatNotificationSender;
import com.rymcu.mortise.wechat.integration.WeChatOAuth2Adapter;
import com.rymcu.mortise.wechat.model.AuthorizationUrlResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 微信认证服务
 * <p>处理微信登录并与现有用户系统集成</p>
 * <p>本服务协调多个底层服务完成完整的登录流程</p>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>完整的微信扫码登录流程（包含 State 验证、用户信息获取、通知发送）</li>
 *     <li>与现有认证体系集成（OAuth2/OIDC）</li>
 *     <li>多公众号场景的账号选择和管理</li>
 * </ul>
 *
 * <h3>调用示例：</h3>
 * <pre>
 * // 1. 生成授权 URL
 * AuthorizationUrlResult result = authService.buildAuthorizationUrl(accountId, redirectUri);
 *
 * // 2. 处理回调
 * WxOAuth2UserInfo userInfo = authService.handleLogin(accountId, code, state);
 *
 * // 3. 发送通知
 * authService.sendLoginSuccessNotification(accountId, userInfo.getOpenid(), username);
 * </pre>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WeChatLoginService.class)
public class WeChatAuthService {

    private final WeChatLoginService weChatLoginService;
    private final WeChatOAuth2Adapter weChatOAuth2Adapter;
    private final WeChatNotificationSender notificationSender;
    private final WeChatCacheService weChatCacheService;

    /**
     * 生成微信授权 URL（PC 端扫码登录）
     *
     * @param accountId   账号ID（null 表示使用默认账号）
     * @param redirectUri 授权后重定向地址
     * @return 授权 URL 和 State
     */
    public AuthorizationUrlResult buildAuthorizationUrl(Long accountId, String redirectUri) {
        String state = generateAndCacheState(accountId);
        String authUrl = weChatLoginService.buildAuthorizationUrl(accountId, redirectUri, state);

        log.info("生成微信授权URL - accountId: {}, state: {}", accountId, state);

        return new AuthorizationUrlResult(authUrl, state);
    }

    /**
     * 生成微信 H5 授权 URL（移动端）
     *
     * @param accountId   账号ID（null 表示使用默认账号）
     * @param redirectUri 授权后重定向地址
     * @return 授权 URL 和 State
     */
    public AuthorizationUrlResult buildH5AuthorizationUrl(Long accountId, String redirectUri) {
        String state = generateAndCacheState(accountId);
        String authUrl = weChatLoginService.buildH5AuthorizationUrl(accountId, redirectUri, state);

        log.info("生成微信H5授权URL - accountId: {}, state: {}", accountId, state);

        return new AuthorizationUrlResult(authUrl, state);
    }

    /**
     * 处理微信登录（完整流程）
     * <p>包含：验证 state、获取用户信息、转换数据格式</p>
     *
     * @param accountId 账号ID（如果为 null，从 state 缓存中获取）
     * @param code      微信授权码
     * @param state     状态码（用于防止 CSRF 攻击）
     * @return 微信用户信息
     * @throws WxErrorException      微信 API 异常
     * @throws IllegalStateException State 验证失败
     */
    public WxOAuth2UserInfo handleLogin(Long accountId, String code, String state) throws WxErrorException {
        log.info("开始处理微信登录 - accountId: {}, code: {}, state: {}", accountId, code, state);

        try {
            // 1. 验证并获取 accountId（从 state 缓存或参数）
            Long finalAccountId = validateStateAndGetAccountId(state, accountId);

            // 2. 通过 LoginService 获取微信用户信息
            WxOAuth2UserInfo userInfo = weChatLoginService.getUserInfoByCode(finalAccountId, code);

            log.info("微信登录成功 - accountId: {}, openId: {}, nickname: {}",
                    finalAccountId, userInfo.getOpenid(), userInfo.getNickname());

            return userInfo;

        } catch (WxErrorException e) {
            log.error("微信登录失败 - code: {}, error: {}", code, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败 - code: {}", code, e);
            throw new RuntimeException("微信登录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理微信登录（使用默认账号）
     *
     * @param code  微信授权码
     * @param state 状态码
     * @return 微信用户信息
     * @throws WxErrorException 微信 API 异常
     */
    public WxOAuth2UserInfo handleLogin(String code, String state) throws WxErrorException {
        return handleLogin(null, code, state);
    }

    /**
     * 获取标准化的 OAuth2 用户信息（用于集成到认证体系）
     *
     * @param accountId 账号ID
     * @param code      微信授权码
     * @param state     状态码
     * @return 标准化用户信息
     * @throws WxErrorException 微信 API 异常
     */
    public Object getStandardUserInfo(Long accountId, String code, String state) throws WxErrorException {
        // 1. 验证并获取用户信息
        Long finalAccountId = validateStateAndGetAccountId(state, accountId);

        // 2. 通过 Adapter 转换为标准格式
        try {
            return weChatOAuth2Adapter.getUserInfoByCode(finalAccountId, code);
        } catch (Exception e) {
            log.warn("StandardOAuth2UserInfo 转换失败，返回原始 WxOAuth2UserInfo: {}", e.getMessage());
            return weChatLoginService.getUserInfoByCode(finalAccountId, code);
        }
    }

    /**
     * 发送登录成功通知
     *
     * @param accountId 账号ID（用于指定哪个公众号发送通知）
     * @param openId    用户 OpenID
     * @param username  用户名
     */
    public void sendLoginSuccessNotification(Long accountId, String openId, String username) {
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        notificationSender.sendWelcomeNotification(accountId, openId, username, currentTime);
    }

    /**
     * 发送登录成功通知（使用默认账号）
     *
     * @param openId   用户 OpenID
     * @param username 用户名
     */
    public void sendLoginSuccessNotification(String openId, String username) {
        sendLoginSuccessNotification(null, openId, username);
    }

    /**
     * 发送安全登录提醒
     *
     * @param accountId 账号ID
     * @param openId    用户 OpenID
     * @param ip        登录 IP
     * @param location  登录地点
     */
    public void sendSecurityAlert(Long accountId, String openId, String ip, String location) {
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        notificationSender.sendLoginNotification(
                accountId,
                openId,
                ip,
                location,
                "微信扫码登录",
                currentTime
        );
    }

    /**
     * 发送安全登录提醒（使用默认账号）
     *
     * @param openId   用户 OpenID
     * @param ip       登录 IP
     * @param location 登录地点
     */
    public void sendSecurityAlert(String openId, String ip, String location) {
        sendSecurityAlert(null, openId, ip, location);
    }

    // ==================== 私有方法 ====================

    /**
     * 生成并缓存 State
     *
     * @param accountId 账号ID
     * @return State 字符串
     */
    private String generateAndCacheState(Long accountId) {
        String state = UUID.randomUUID().toString();

        weChatCacheService.cacheAuthState(state, accountId);
        log.debug("生成并缓存 State - state: {}, accountId: {}", state, accountId);

        return state;
    }

    /**
     * 验证 State 并获取 AccountId
     *
     * @param state          State 字符串
     * @param paramAccountId 参数中的 AccountId（可选）
     * @return 最终使用的 AccountId
     * @throws IllegalStateException State 验证失败
     */
    private Long validateStateAndGetAccountId(String state, Long paramAccountId) {

        try {
            Long cachedAccountId = weChatCacheService.validateAndGetAccountId(state);

            // 如果参数中也提供了 accountId，验证一致性
            if (paramAccountId != null && cachedAccountId != null && !paramAccountId.equals(cachedAccountId)) {
                log.warn("AccountId 不一致 - 缓存: {}, 参数: {}, 使用缓存值", cachedAccountId, paramAccountId);
            }

            // 优先使用缓存中的 accountId
            return cachedAccountId != null ? cachedAccountId : paramAccountId;

        } catch (IllegalStateException e) {
            log.error("State 验证失败: {}", e.getMessage());
            throw e;
        }
    }
}
