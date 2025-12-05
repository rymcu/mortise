package com.rymcu.mortise.auth.service.impl;

import com.rymcu.mortise.auth.model.TokenModel;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通过统一缓存服务存储和验证token的实现类
 * 优化版本：使用 CacheService 统一管理缓存操作
 *
 * @author ronger
 * @date 2024/04/13.
 */
@Slf4j
@Component
public class CacheTokenManager implements TokenManager {

    @Resource
    private AuthCacheService authCacheService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 生成 ACCESS TOKEN
     * <p>
     * 功能:
     * 1. 参数验证
     * 2. 生成 JWT Token
     * 3. 缓存 Token（支持单点登录控制）
     * 4. 详细日志记录
     *
     * @param account 用户账号
     * @return JWT Token
     * @throws IllegalArgumentException 当账号为空时抛出
     */
    @Override
    public String createToken(String account) {
        // 1. 参数验证
        if (account == null || account.trim().isEmpty()) {
            log.error("创建 Token 失败: 账号不能为空");
            throw new IllegalArgumentException("账号不能为空");
        }

        try {
            // 2. 清理旧的 Token（可选：实现单点登录）
            String existingToken = authCacheService.getJwtToken(account);
            if (existingToken != null) {
                log.debug("检测到用户已有 Token，将被新 Token 替换: account={}", account);
            }

            // 3. 生成新的 JWT Token
            String newToken = jwtTokenUtil.generateToken(account.trim());
            if (newToken == null) {
                log.error("JWT Token 生成失败: account={}", account);
                throw new RuntimeException("Token 生成失败");
            }

            // 4. 缓存新 Token
            authCacheService.storeJwtToken(account.trim(), newToken);
            log.info("创建并缓存 Token 成功: account={}, tokenLength={}", account, newToken.length());
            return newToken;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建 Token 时发生异常: account={}", account, e);
            throw new RuntimeException("Token 创建失败", e);
        }
    }

    /**
     * 解析 TOKEN 模型
     * <p>
     * 功能:
     * 1. 参数验证
     * 2. Token 基础格式验证
     * 3. 构建 TokenModel
     *
     * @param token JWT Token 字符串
     * @param account 用户账号
     * @return TokenModel 对象
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @Override
    public TokenModel getToken(String token, String account) {
        // 1. 参数验证
        if (token == null || token.trim().isEmpty()) {
            log.warn("获取 Token 模型失败: Token 不能为空");
            throw new IllegalArgumentException("Token 不能为空");
        }
        
        if (account == null || account.trim().isEmpty()) {
            log.warn("获取 Token 模型失败: 账号不能为空");
            throw new IllegalArgumentException("账号不能为空");
        }

        try {
            // 2. Token 基础格式验证（JWT 应该包含两个点）
            String cleanToken = token.trim();
            if (cleanToken.split("\\.").length != 3) {
                log.warn("Token 格式无效: account={}, tokenPrefix={}", account, 
                    cleanToken.length() > 20 ? cleanToken.substring(0, 20) + "..." : cleanToken);
                throw new IllegalArgumentException("Token 格式无效");
            }

            // 3. 验证 Token 中的用户名是否匹配
            String tokenUsername = jwtTokenUtil.getUsernameFromToken(cleanToken);
            if (tokenUsername == null || !tokenUsername.equals(account.trim())) {
                log.warn("Token 中的用户名与提供的账号不匹配: expected={}, actual={}", account, tokenUsername);
                throw new IllegalArgumentException("Token 与账号不匹配");
            }

            log.debug("Token 模型创建成功: account={}", account);
            return new TokenModel(account.trim(), cleanToken);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建 Token 模型时发生异常: account={}", account, e);
            throw new RuntimeException("Token 模型创建失败", e);
        }
    }

    /**
     * 验证 TOKEN 有效性
     * <p>
     * 验证流程:
     * 1. 基础参数验证
     * 2. JWT Token 自身验证（签名、过期等）
     * 3. 缓存一致性验证（防止已注销 Token）
     * 4. 用户名匹配验证
     *
     * @param model Token 模型
     * @return true: Token 有效, false: Token 无效
     */
    @Override
    public boolean checkToken(TokenModel model) {
        // 1. 基础参数验证
        if (model == null) {
            log.debug("Token 验证失败: TokenModel 为 null");
            return false;
        }

        if (model.getToken() == null || model.getToken().trim().isEmpty()) {
            log.debug("Token 验证失败: Token 为空");
            return false;
        }

        if (model.getUsername() == null || model.getUsername().trim().isEmpty()) {
            log.debug("Token 验证失败: 用户名为空");
            return false;
        }

        String token = model.getToken().trim();
        String username = model.getUsername().trim();

        try {
            // 2. JWT Token 自身验证（签名、过期、格式等）
            if (!jwtTokenUtil.validateToken(token, username)) {
                log.debug("Token 验证失败: JWT 验证不通过, username={}", username);
                return false;
            }

            // 3. 缓存一致性验证（确保 Token 未被注销）
            String cachedToken = authCacheService.getJwtToken(username);
            if (cachedToken == null) {
                log.debug("Token 验证失败: 缓存中不存在该用户的 Token, username={}", username);
                return false;
            }

            if (!cachedToken.equals(token)) {
                log.debug("Token 验证失败: Token 与缓存不匹配（可能已被替换或注销）, username={}", username);
                return false;
            }

            // 4. 所有验证通过
            log.debug("Token 验证成功: username={}", username);
            return true;

        } catch (Exception e) {
            log.warn("Token 验证过程中发生异常: username={}", username, e);
            return false;
        }
    }

    @Override
    public void deleteToken(String account) {
        // 使用统一缓存服务删除token
        Boolean deleted = authCacheService.removeJwtToken(account);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("删除 Token 成功: account={}", account);
        }
    }

    @Override
    public void revokeToken(String account, String token) {
        try {
            // 1. 从 Token 中获取 jti
            String jti = jwtTokenUtil.getJtiFromToken(token);
            
            if (jti != null) {
                // 2. 计算 Token 剩余有效期（秒）
                java.util.Date expiration = jwtTokenUtil.getExpirationDateFromToken(token);
                long expireInSeconds = 0;
                if (expiration != null) {
                    expireInSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                    if (expireInSeconds < 0) {
                        expireInSeconds = 0; // Token 已过期，无需加入黑名单
                    }
                }
                
                // 3. 将 jti 加入黑名单
                if (expireInSeconds > 0) {
                    authCacheService.addToBlacklist(jti, expireInSeconds);
                    log.info("Token 已加入黑名单: account={}, jti={}, 剩余有效期={}秒", account, jti, expireInSeconds);
                }
            } else {
                log.warn("无法获取 Token 的 jti，跳过黑名单处理: account={}", account);
            }
            
            // 4. 从缓存中删除 Token
            deleteToken(account);
            
        } catch (Exception e) {
            log.error("注销 Token 时发生异常: account={}", account, e);
            // 即使出错也要尝试删除缓存中的 Token
            deleteToken(account);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        try {
            String jti = jwtTokenUtil.getJtiFromToken(token);
            if (jti == null) {
                // 如果无法获取 jti，保守处理，不认为已注销
                // 这种情况可能是旧版本生成的 Token（没有 jti）
                log.debug("Token 没有 jti，跳过黑名单检查");
                return false;
            }
            return authCacheService.isBlacklisted(jti);
        } catch (Exception e) {
            log.warn("检查 Token 黑名单时发生异常", e);
            return false;
        }
    }

    @Override
    public String refreshAccessToken(String oldToken, String account) {
        try {
            // 1. 验证旧 Token 是否属于该用户
            if (!jwtTokenUtil.validateToken(oldToken, account)) {
                log.warn("Token 验证失败，无法刷新: account={}", account);
                return null;
            }

            // 2. 检查缓存中的 Token 是否匹配（防止使用已注销的 Token）
            String cachedToken = authCacheService.getJwtToken(account);
            if (cachedToken == null || !cachedToken.equals(oldToken)) {
                log.warn("Token 不匹配或已失效，无法刷新: account={}", account);
                return null;
            }

            // 3. 使用 JwtTokenUtil 刷新 Token
            String newToken = jwtTokenUtil.refreshToken(oldToken);
            if (newToken == null) {
                log.warn("Token 刷新失败（可能未到刷新窗口期）: account={}", account);
                return null;
            }

            // 4. 更新缓存中的 Token
            authCacheService.storeJwtToken(account, newToken);
            log.info("Token 刷新成功: account={}", account);
            return newToken;

        } catch (Exception e) {
            log.error("刷新 Token 时发生异常: account={}", account, e);
            return null;
        }
    }
}
