package com.rymcu.mortise.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    @Value("${jwt.refresh-window:300000}") // 默认过期前 5 分钟才能刷新
    private long refreshWindow;

    /**
     * -- GETTER --
     *  获取 Token 请求头名称
     */
    @Getter
    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    /**
     * -- GETTER --
     *  获取 Token 前缀
     */
    @Getter
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
     * 从 Token 中获取 JWT ID (jti)
     * <p>
     * jti 是每个 Token 的唯一标识符，用于：
     * <ul>
     *   <li>Token 黑名单管理（注销 Token）</li>
     *   <li>防止 Token 重放攻击</li>
     *   <li>日志追踪和审计</li>
     * </ul>
     *
     * @param token JWT Token
     * @return Token 的唯一标识符 (jti)
     */
    public String getJtiFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
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
     * <p>
     * 使用缓存的 SecretKey 提高性能。
     * 每个 Token 都会生成唯一的 jti (JWT ID)，用于：
     * <ul>
     *   <li>Token 黑名单管理（注销 Token）</li>
     *   <li>防止 Token 重放攻击</li>
     *   <li>日志追踪和审计</li>
     * </ul>
     *
     * @param claims  自定义声明
     * @param subject 用户标识（通常是用户名或用户ID）
     * @return 生成的 JWT Token
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString(); // 生成唯一的 Token ID
        
        return Jwts.builder()
                .claims(claims)
                .id(jti) // 添加 jti，用于 Token 黑名单和追踪
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
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
     * <p>
     * 安全策略:
     * 1. 只允许在 Token 即将过期但尚未过期时刷新
     * 2. 设置刷新窗口期（默认过期前 5 分钟内才能刷新）
     * 3. 保留必要的自定义 Claims，但重新生成时间相关的标准 Claims
     *
     * @param token 待刷新的 Token
     * @return 新的 Token，如果刷新失败则返回 null
     */
    public String refreshToken(String token) {
        try {
            // 1. 验证 Token 是否已过期
            if (isTokenExpired(token)) {
                log.warn("无法刷新已过期的 Token");
                return null;
            }

            // 2. 检查是否在刷新窗口期内
            Date expirationDate = getExpirationDateFromToken(token);
            if (expirationDate != null) {
                long timeUntilExpiration = expirationDate.getTime() - System.currentTimeMillis();
                if (timeUntilExpiration > refreshWindow) {
                    log.debug("Token 尚未进入刷新窗口期，剩余有效时间: {} ms", timeUntilExpiration);
                    // 如果还没到刷新窗口期，返回 null 表示暂时不需要刷新
                    // 调用方可以继续使用原 Token
                    return null;
                }
            }

            // 3. 解析 Token 获取 Claims
            final Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                log.warn("无法从 Token 中解析 Claims");
                return null;
            }

            // 4. 提取用户名
            String username = claims.getSubject();
            if (username == null || username.trim().isEmpty()) {
                log.warn("Token 中缺少有效的 subject");
                return null;
            }

            // 5. 提取自定义 Claims（排除 JWT 标准 Claims）
            Map<String, Object> customClaims = new HashMap<>();
            claims.forEach((key, value) -> {
                if (!isStandardClaim(key)) {
                    customClaims.put(key, value);
                }
            });

            // 6. 生成新的 Token（会自动设置新的 iat 和 exp）
            String newToken = generateToken(username, customClaims);
            log.info("Token 刷新成功，用户: {}", username);
            return newToken;

        } catch (Exception e) {
            log.error("Token 刷新失败", e);
            return null;
        }
    }

    /**
     * 判断是否为 JWT 标准 Claim
     * <p>
     * 标准 Claims 包括: iss, sub, aud, exp, nbf, iat, jti
     *
     * @param claimName Claim 名称
     * @return true: 标准 Claim, false: 自定义 Claim
     */
    private boolean isStandardClaim(String claimName) {
        return Claims.ISSUER.equals(claimName)
                || Claims.SUBJECT.equals(claimName)
                || Claims.AUDIENCE.equals(claimName)
                || Claims.EXPIRATION.equals(claimName)
                || Claims.NOT_BEFORE.equals(claimName)
                || Claims.ISSUED_AT.equals(claimName)
                || Claims.ID.equals(claimName);
    }

}
