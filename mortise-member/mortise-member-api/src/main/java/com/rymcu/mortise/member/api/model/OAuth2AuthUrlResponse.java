package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2 授权 URL 响应对象
 * 用于手机端页面重定向授权场景
 *
 * @author ronger
 */
@Schema(description = "OAuth2 授权 URL 响应")
public record OAuth2AuthUrlResponse(
        @Schema(description = "授权 URL，客户端可直接跳转此链接进行授权",
                example = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=...&redirect_uri=...&response_type=code&scope=snsapi_userinfo&state=...")
        String authorizationUrl,
        @Schema(description = "状态码，用于验证回调请求的合法性", example = "uuid-string")
        String state,
        @Schema(description = "应用ID", example = "wx1234567890abcdef")
        String appId,
        @Schema(description = "重定向 URI", example = "http://your-domain.com/api/v1/app/oauth2/callback/wechat")
        String redirectUri,
        @Schema(description = "授权作用域", example = "snsapi_userinfo")
        String scope,
        @Schema(description = "授权类型：redirect（页面重定向）或 qrcode（扫码）", example = "redirect")
        String authType
) {
}
