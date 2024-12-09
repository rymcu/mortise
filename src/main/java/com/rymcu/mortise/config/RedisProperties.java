package com.rymcu.mortise.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author ronger
 */
@Getter
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties extends JedisPoolConfig {
    private String host = Protocol.DEFAULT_HOST;
    @Setter
    private int port = Protocol.DEFAULT_PORT;
    private String password;
    @Setter
    private int database = 1;
    @Setter
    private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
    @Setter
    private int soTimeout = Protocol.DEFAULT_TIMEOUT;
    private String clientName;
    @Setter
    private boolean ssl;
    @Setter
    private SSLSocketFactory sslSocketFactory;
    @Setter
    private SSLParameters sslParameters;
    @Setter
    private HostnameVerifier hostnameVerifier;

    public void setHost(String host) {
        if (StringUtils.isBlank(host)) {
            host = Protocol.DEFAULT_HOST;
        }
        this.host = host;
    }

    public void setPassword(String password) {
        if (StringUtils.isBlank(password)) {
            password = null;
        }
        this.password = password;
    }

    public void setClientName(String clientName) {
        if (StringUtils.isBlank(clientName)) {
            clientName = null;
        }
        this.clientName = clientName;
    }

}
