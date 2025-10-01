package com.rymcu.mortise.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 连接配置属性
 * 提供 Redis 连接的基础配置参数
 * 
 * @author ronger
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private String host = "localhost";

    private int port = 6379;

    private String password;

    private int database = 0;

    private int connectionTimeout = 2000;

    private int soTimeout = 2000;

    private String clientName;

    // 连接池配置
    private Pool pool = new Pool();

    public void setHost(String host) {
        this.host = StringUtils.isBlank(host) ? "localhost" : host.trim();
    }

    public void setPassword(String password) {
        this.password = StringUtils.isBlank(password) ? null : password;
    }

    /**
     * 连接池配置
     */
    @Getter
    @Setter
    public static class Pool {
        private int maxActive = 8;
        private int maxIdle = 8;
        private int minIdle = 0;
        private long maxWait = -1;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean testWhileIdle = false;
    }
}