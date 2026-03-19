package com.rymcu.mortise.system.model.auth;

/**
 * 登录页 OAuth2 提供商展示信息（不含敏感字段）
 *
 * @param registrationId 客户端注册ID，用于发起授权跳转
 * @param clientName     展示给用户的名称
 * @param icon           Iconify 图标类名，如 i-simple-icons-github
 */
public record OAuth2ProviderInfo(String registrationId, String clientName, String icon) {
}
