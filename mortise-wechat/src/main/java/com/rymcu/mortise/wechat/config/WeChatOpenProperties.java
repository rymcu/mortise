package com.rymcu.mortise.wechat.config;

import lombok.Data;

/**
 * 微信开放平台配置属性（用于网站扫码登录）
 * <p>注意：此类不是 Spring Bean，而是由 WeChatMultiAccountConfigService 从数据库创建</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
public class WeChatOpenProperties {

    /**
     * 开放平台 AppID
     */
    private String appId;

    /**
     * 开放平台 AppSecret
     */
    private String secret;

    /**
     * 授权回调地址
     */
    private String redirectUri;

    /**
     * 是否启用微信开放平台登录
     */
    private boolean enabled = false;

    /**
     * 扫码登录二维码有效期（秒），默认 5 分钟
     */
    private int qrCodeExpireSeconds = 300;

    // 兼容旧配置名称
    public String getAppSecret() {
        return secret;
    }

    public void setAppSecret(String appSecret) {
        this.secret = appSecret;
    }
}
