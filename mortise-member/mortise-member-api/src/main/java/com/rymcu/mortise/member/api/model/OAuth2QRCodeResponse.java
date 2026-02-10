package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2 二维码响应对象
 *
 * @author ronger
 */
@Schema(description = "OAuth2 二维码响应")
public record OAuth2QRCodeResponse(
        @Schema(description = "授权 URL", example = "https://open.weixin.qq.com/connect/qrconnect?appid=...&redirect_uri=...&response_type=code&scope=snsapi_login&state=...")
        String authorizationUri,
        @Schema(description = "状态码，用于验证回调请求的合法性", example = "uuid-string")
        String state,
        @Schema(description = "应用ID", example = "wx1234567890abcdef")
        String appId,
        @Schema(description = "重定向 URI", example = "http://your-domain.com/api/v1/app/oauth2/callback/wechat")
        String redirectUri,
        @Schema(description = "授权作用域", example = "snsapi_login")
        String scope
) {
}
