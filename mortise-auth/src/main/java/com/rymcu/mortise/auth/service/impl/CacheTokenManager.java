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
     * 生成TOKEN
     */
    @Override
    public String createToken(String id) {
        // 使用 account 作为源 token
        String token = jwtTokenUtil.generateToken(id);

        // 使用统一缓存服务存储token
        authCacheService.storeJwtToken(id, token);
        log.debug("创建并缓存 Token 成功: account={}", id);
        return token;
    }

    @Override
    public TokenModel getToken(String token, String account) {
        return new TokenModel(account, token);
    }

    @Override
    public boolean checkToken(TokenModel model) {
        if (model == null) {
            return false;
        }

        // 使用统一缓存服务获取token
        String cachedToken = authCacheService.getJwtToken(model.getUsername());
        return cachedToken != null && cachedToken.equals(model.getToken());
    }

    @Override
    public void deleteToken(String account) {
        // 使用统一缓存服务删除token
        Boolean deleted = authCacheService.removeJwtToken(account);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("删除 Token 成功: account={}", account);
        }
    }
}
