package com.rymcu.mortise.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token 工具类 (优化版)
 * <p>
 * 提供 JWT 的生成、解析、验证功能
 * <p>
 * 优化点:
 * 1. 缓存 SecretKey 和 JwtParser 以提高性能.
 * 2. 补全 getClaimFromToken 方法.
 * 3. 增加更具体的异常捕获和日志记录.
 *
 * @author ronger
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:mortise-secret-key-please-change-this-in-production-environment}")
    private String secret;

    @Value("${jwt.expiration:1800000}") // 默认 30 分钟
    private long expiration;

    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    /**
     * 在 Bean 初始化后执行, 用于生成 SecretKey 和 JwtParser.
     * 这避免了在每次操作时都重复创建, 提高了性能.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    /**
     * 从 Token 中获取用户名 (Subject)
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从 Token 中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从 Token 中获取指定的声明 (Claim)
     *
     * @param token          JWT Token
     * @param claimsResolver 用于从 Claims 中提取信息的函数
     * @param <T>            声明值的类型
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    /**
     * 从 Token 中获取所有声明
     * 使用缓存的 JwtParser 提高性能
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 Token 格式: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Token 签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token 参数错误: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析 Token 失败", e);
        }
        return null;
    }

    /**
     * 检查 Token 是否过期
     *
     * @return true: 已过期, false: 未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expirationDate = getExpirationDateFromToken(token);
            if (expirationDate == null) {
                return true;
            }
            return expirationDate.before(new Date());
        } catch (Exception e) {
            // 如果在获取过期时间时发生任何解析异常, 都认为 Token 是无效或过期的
            return true;
        }
    }

    /**
     * 为指定用户生成 Token
     */
    public String generateToken(String username) {
        return doGenerateToken(new HashMap<>(), username);
    }

    /**
     * 为指定用户生成 Token（带自定义声明）
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return doGenerateToken(claims, username);
    }

    /**
     * 执行 Token 生成
     * 使用缓存的 SecretKey 提高性能
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(createdDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证 Token 是否有效
     * 1. 用户名是否匹配
     * 2. Token 是否过期
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        // 用户名可能为 null (如果 token 解析失败)
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * 刷新 Token
     * 保持原始声明, 只更新签发和过期时间
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                return null;
            }
            // JJWT 0.12+ Claims 是不可变的, 需要基于它重新创建
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            log.error("Token 刷新失败", e);
            return null;
        }
    }

    /**
     * 计算过期时间
     */
    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + expiration);
    }

    /**
     * 获取 Token 请求头名称
     */
    public String getTokenHeader() {
        return tokenHeader;
    }

    /**
     * 获取 Token 前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }
}
