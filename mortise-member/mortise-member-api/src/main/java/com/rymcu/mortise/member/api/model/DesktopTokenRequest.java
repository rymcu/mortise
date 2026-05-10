package com.rymcu.mortise.member.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 桌面端 Token 请求。
 *
 * @param grantType    授权类型
 * @param code         授权码
 * @param redirectUri  回调地址
 * @param codeVerifier PKCE verifier
 * @param refreshToken 刷新令牌
 */
public record DesktopTokenRequest(
        @NotBlank @JsonProperty("grant_type") String grantType,
        String code,
        @JsonProperty("redirect_uri") String redirectUri,
        @JsonProperty("code_verifier") String codeVerifier,
        @JsonProperty("refresh_token") String refreshToken
) {
}
