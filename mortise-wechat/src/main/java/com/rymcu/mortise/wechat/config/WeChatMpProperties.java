package com.rymcu.mortise.wechat.config;

import lombok.Data;

import java.util.List;

/**
 * 微信公众号配置属性
 * <p>支持多账号配置，从数据库动态加载</p>
 * <p>注意：此类不是 Spring Bean，而是由 WeChatMultiAccountConfigService 从数据库创建</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
public class WeChatMpProperties {

    /**
     * 是否使用 Redis 存储 access token
     * -- GETTER --
     *  是否使用 Redis

     */
    private boolean useRedis = false;

    /**
     * Redis 配置
     * -- GETTER --
     *  获取 Redis 配置

     */
    private RedisConfig redisConfig;

    /**
     * 多账号配置列表
     * -- GETTER --
     *  获取配置列表
     * -- SETTER --
     *  设置配置列表


     */
    private List<Config> configs;

    /**
     * 单账号配置（兼容旧版本）
     * -- GETTER --
     *  获取 AppID（兼容旧配置）

     */
    private String appId;
    private String secret;
    /**
     * -- GETTER --
     *  获取 Token（兼容旧配置）
     */
    private String token;
    /**
     * -- GETTER --
     *  获取 AES Key（兼容旧配置）
     */
    private String aesKey;
    /**
     * -- GETTER --
     *  是否启用（兼容旧配置）
     */
    private boolean enabled = false;

    /**
     * Redis 配置类
     */
    @Data
    public static class RedisConfig {
        /**
         * Redis 服务器主机地址
         * -- GETTER --
         *  获取主机地址

         */
        private String host;

        /**
         * Redis 服务器端口
         * -- GETTER --
         *  获取端口

         */
        private int port;

        /**
         * Redis 连接超时时间
         * -- GETTER --
         *  获取超时时间

         */
        private int timeout;

        /**
         * Redis 连接密码
         * -- GETTER --
         *  获取连接密码

         */
        private String password;

    }

    /**
     * 微信公众号单个账号配置
     */
    @Data
    public static class Config {
        /**
         * 公众号 AppID
         * -- GETTER --
         *  获取 AppID

         */
        private String appId;

        /**
         * 公众号 AppSecret
         * -- GETTER --
         *  获取 Secret

         */
        private String secret;

        /**
         * 公众号 Token（消息验证）
         * -- GETTER --
         *  获取 Token

         */
        private String token;

        /**
         * 公众号 AES Key（消息加解密）
         * -- GETTER --
         *  获取 AES Key

         */
        private String aesKey;

        /**
         * 是否启用该账号
         * -- SETTER --
         *  设置启用状态
         * -- GETTER --
         *  是否启用该配置


         */
        private boolean enabled = true;

        /**
         * 账号名称（可选，用于标识）
         * -- GETTER --
         *  获取账号名称

         */
        private String accountName;

        // 兼容旧配置名称
        public String getAppSecret() {
            return secret;
        }

        public void setAppSecret(String appSecret) {
            this.secret = appSecret;
        }

    }

    // 兼容旧配置名称
    public String getAppSecret() {
        return secret;
    }

    public void setAppSecret(String appSecret) {
        this.secret = appSecret;
    }

    /**
     * 获取所有启用的配置
     */
    public List<Config> getEnabledConfigs() {
        if (configs == null || configs.isEmpty()) {
            return List.of();
        }
        return configs.stream()
                .filter(Config::isEnabled)
                .toList();
    }

    /**
     * 根据 AppID 获取配置
     */
    public Config getConfigByAppId(String appId) {
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        return configs.stream()
                .filter(config -> appId.equals(config.getAppId()))
                .findFirst()
                .orElse(null);
    }

}
