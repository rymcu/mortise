package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 微信静默授权 openid 响应
 *
 * @author ronger
 * @since 1.0.0
 */
@Schema(description = "微信静默授权 OpenId 响应")
public record WeChatOpenIdResponse(
        @Schema(description = "微信用户 openid")
        String openId,
        @Schema(description = "是否已绑定平台会员")
        Boolean isBound,
        @Schema(description = "已绑定的会员ID（未绑定时为空）")
        Long memberId
) {
}
