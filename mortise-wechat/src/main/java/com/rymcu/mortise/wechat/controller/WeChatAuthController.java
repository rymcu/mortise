package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.wechat.service.WeChatAuthService;
import com.rymcu.mortise.wechat.model.AuthorizationUrlResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信扫码登录控制器
 * <p>基于 WeChatAuthService 实现完整的认证流程，包括 State 验证、通知发送等</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wechat/auth")
@RequiredArgsConstructor
@ConditionalOnBean(WeChatAuthService.class)
@Tag(name = "微信登录", description = "微信扫码登录和授权相关接口")
public class WeChatAuthController {

    private final WeChatAuthService weChatAuthService;

    /**
     * 获取微信扫码登录 URL（PC 端）
     * <p>使用 WeChatAuthService 生成带 State 验证的授权 URL</p>
     * <p>State 会缓存到 Redis/Caffeine，有效期 10 分钟，用于回调时验证防止 CSRF 攻击</p>
     *
     * <h3>安全说明：</h3>
     * <ul>
     *     <li>redirectUri 应该是<b>前端页面地址</b>（如：<a href="https://your-frontend.com/login-callback">...</a>）</li>
     *     <li>微信授权后会重定向到该地址，并在 URL 中携带 code 和 state 参数</li>
     *     <li>前端需要提取 code 和 state，然后调用 /callback 接口完成登录</li>
     *     <li><b>不要</b>直接将 redirectUri 设置为后端 /callback 接口，避免 Token 暴露在 URL 中</li>
     * </ul>
     *
     * <h3>前端集成示例：</h3>
     * <pre>
     * // 1. 获取授权 URL
     * const { authUrl, state } = await fetch('/api/v1/wechat/auth/qrcode-url?redirectUri=<a href="https://your-frontend.com/login-callback">...</a>');
     * sessionStorage.setItem('wechat_state', state);
     * window.location.href = authUrl;
     *
     * // 2. 在回调页面提取参数并调用 callback
     * const code = new URLSearchParams(window.location.search).get('code');
     * const state = new URLSearchParams(window.location.search).get('state');
     * const result = await fetch(`/api/v1/wechat/auth/callback?code=${code}&state=${state}`);
     * </pre>
     *
     * @param redirectUri 授权后重定向地址（<b>必须是前端页面地址</b>，如：<a href="https://your-frontend.com/login-callback">...</a>）
     * @param accountId   账号ID（可选，不传则使用默认账号）
     * @return 微信授权 URL 和 State
     */
    @Operation(summary = "获取微信扫码登录URL", description = "获取PC端微信扫码登录的授权URL（包含State验证）。redirectUri必须是前端页面地址，不要设置为后端接口。")
    @GetMapping("/qrcode-url")
    public GlobalResult<Map<String, String>> getQrCodeUrl(
            @Parameter(description = "授权后重定向地址（前端页面URL，非后端接口）", required = true, example = "https://your-frontend.com/login-callback")
            @RequestParam String redirectUri,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {

        // 使用 WeChatAuthService 生成并缓存 State
        AuthorizationUrlResult result =
                weChatAuthService.buildAuthorizationUrl(accountId, redirectUri);

        Map<String, String> response = new HashMap<>();
        response.put("authUrl", result.authUrl());
        response.put("state", result.state());

        log.info("生成微信扫码登录URL - accountId: {}, state: {}", accountId, result.state());

        return GlobalResult.success(response);
    }

    /**
     * 获取微信 H5 授权 URL
     * <p>使用 WeChatAuthService 生成带 State 验证的 H5 授权 URL</p>
     * <p>State 会缓存到 Redis/Caffeine，有效期 10 分钟，用于回调时验证防止 CSRF 攻击</p>
     *
     * <h3>安全说明：</h3>
     * <ul>
     *     <li>redirectUri 应该是<b>前端页面地址</b>（如：<a href="https://your-frontend.com/login-callback">...</a>）</li>
     *     <li>微信授权后会重定向到该地址，并在 URL 中携带 code 和 state 参数</li>
     *     <li>前端需要提取 code 和 state，然后调用 /callback 接口完成登录</li>
     *     <li><b>不要</b>直接将 redirectUri 设置为后端 /callback 接口，避免 Token 暴露在 URL 中</li>
     * </ul>
     *
     * <h3>前端集成示例：</h3>
     * <pre>
     * // 1. 获取授权 URL
     * const { authUrl, state } = await fetch('/api/v1/wechat/auth/h5-url?redirectUri=https://your-frontend.com/login-callback');
     * sessionStorage.setItem('wechat_state', state);
     * window.location.href = authUrl;
     *
     * // 2. 在回调页面提取参数并调用 callback
     * const code = new URLSearchParams(window.location.search).get('code');
     * const state = new URLSearchParams(window.location.search).get('state');
     * const result = await fetch(`/api/v1/wechat/auth/callback?code=${code}&state=${state}`);
     * </pre>
     *
     * @param redirectUri 授权后重定向地址（<b>必须是前端页面地址</b>，如：<a href="https://your-frontend.com/login-callback">...</a>）
     * @param accountId   账号ID（可选，不传则使用默认账号）
     * @return 微信授权 URL 和 State
     */
    @Operation(summary = "获取微信H5授权URL", description = "获取移动端微信H5授权URL（包含State验证）。redirectUri必须是前端页面地址，不要设置为后端接口。")
    @GetMapping("/h5-url")
    public GlobalResult<Map<String, String>> getH5Url(
            @Parameter(description = "授权后重定向地址（前端页面URL，非后端接口）", required = true, example = "https://your-frontend.com/login-callback")
            @RequestParam String redirectUri,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {

        // 使用 WeChatAuthService 生成并缓存 State
        AuthorizationUrlResult result =
                weChatAuthService.buildH5AuthorizationUrl(accountId, redirectUri);

        Map<String, String> response = new HashMap<>();
        response.put("authUrl", result.authUrl());
        response.put("state", result.state());

        log.info("生成微信H5授权URL - accountId: {}, state: {}", accountId, result.state());

        return GlobalResult.success(response);
    }

}
