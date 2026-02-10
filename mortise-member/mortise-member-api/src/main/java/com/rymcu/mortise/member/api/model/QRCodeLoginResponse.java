package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 二维码登录响应
 *
 * @author ronger
 * @since 1.0.0
 */
@Schema(description = "二维码登录响应")
public record QRCodeLoginResponse(
        @Schema(description = "场景值（用于轮询状态）", example = "LOGIN_uuid-string")
        String sceneStr,
        @Schema(description = "二维码图片 URL", example = "http://weixin.qq.com/q/kZgfwMTm72WWPkovabbI")
        String qrCodeUrl,
        @Schema(description = "二维码 ticket（可用于换取二维码图片）", example = "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGlu...")
        String ticket,
        @Schema(description = "二维码有效期（秒）", example = "300")
        Integer expireSeconds,
        @Schema(description = "二维码图片显示 URL（用于前端直接显示）",
                example = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=xxx")
        String showQrCodeUrl
) {
}
