package com.rymcu.mortise.member.api.model;

/**
 * 桌面端授权响应。
 *
 * @param redirectUri 回调地址
 */
public record DesktopAuthorizeResponse(String redirectUri) {
}
