package com.rymcu.mortise.auth;

import com.rymcu.mortise.handler.event.AccountEvent;
import com.rymcu.mortise.service.CacheService;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 通过统一缓存服务存储和验证token的实现类
 * 优化版本：使用 CacheService 统一管理缓存操作
 *
 * @author ronger
 * @date 2024/04/13.
 */
@Component
public class CacheTokenManager implements TokenManager {

    @Resource
    private CacheService cacheService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 生成TOKEN
     */
    @Override
    public String createToken(String id) {
        // 生成密钥
        SecretKey key = JwtUtils.getSecretKey();
        // 使用 account 作为源 token
        Date now = new Date();
        String token = Jwts.builder()
                .id(id)
                .subject(id)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + JwtConstants.TOKEN_EXPIRES_MINUTE * 60 * 1000))
                .signWith(key)
                .compact();

        // 使用统一缓存服务存储token
        cacheService.storeJwtToken(id, token);
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
        String cachedToken = cacheService.getJwtToken(model.getUsername());
        if (cachedToken == null || !cachedToken.equals(model.getToken())) {
            return false;
        }

        // 检查和更新最后在线时间
        String lastOnlineTime = cacheService.getUserOnlineStatus(model.getUsername());
        if (StringUtils.isBlank(lastOnlineTime)) {
            // 更新最后在线时间
            applicationEventPublisher.publishEvent(new AccountEvent(model.getUsername()));
            cacheService.storeUserOnlineStatus(model.getUsername(), LocalDateTime.now().toString());
        }

        return true;
    }

    @Override
    public void deleteToken(String account) {
        // 使用统一缓存服务删除token
        cacheService.removeJwtToken(account);
    }
}
