package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2 二维码状态响应
 *
 * @author ronger
 */
@Schema(description = "OAuth2 二维码状态响应")
public record OAuth2QRCodeStateResponse(
        @Schema(description = "状态码（场景值）")
        String state,
        @Schema(description = "二维码状态：0-待授权/待扫描 1-已扫码 2-已授权 3-已取消 4-已过期 -1-状态不存在")
        int qrcodeState
) {
}
