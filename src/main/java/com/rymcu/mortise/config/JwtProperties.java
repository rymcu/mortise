package com.rymcu.mortise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 *
 * @author ronger
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT 密钥
     */
    private String secret;
    
    /**
     * JWT 过期时间（毫秒）
     */
    private Long expiration = 3600000L;
    
    /**
     * 刷新令牌过期时间（毫秒）
     */
    private Long refreshExpiration = 7200000L;
    
    /**
     * Token 请求头名称
     */
    private String header = "Authorization";
    
    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";
}
