package com.rymcu.mortise.auth;


import com.rymcu.mortise.handler.event.AccountEvent;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * 通过Redis存储和验证token的实现类
 *
 * @author ronger
 * @date 2024/04/13.
 */
@Component
public class RedisTokenManager implements TokenManager {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 生成TOKEN
     */
    @Override
    public String createToken(String id) {
        // 生成密钥
        SecretKey key = JwtUtils.getSecretKey();
        //使用 account 作为源 token
        Date now = new Date();
        String token = Jwts.builder().id(id).subject(id).issuedAt(now).expiration(new Date(now.getTime() + JwtConstants.TOKEN_EXPIRES_MINUTE * 60 * 1000)).signWith(key).compact();
        //存储到 redis 并设置过期时间
        redisTemplate.boundValueOps(id).set(token, JwtConstants.TOKEN_EXPIRES_MINUTE, TimeUnit.MINUTES);
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
        String token = redisTemplate.boundValueOps(model.getUsername()).get();
        if (token == null || !token.equals(model.getToken())) {
            return false;
        }
        StringBuilder key = new StringBuilder();
        key.append(JwtConstants.LAST_ONLINE).append(model.getUsername());
        String result = redisTemplate.boundValueOps(key.toString()).get();
        if (StringUtils.isBlank(result)) {
            // 更新最后在线时间
            applicationEventPublisher.publishEvent(new AccountEvent(model.getUsername()));
            redisTemplate.boundValueOps(key.toString()).set(LocalDateTime.now().toString(), JwtConstants.LAST_ONLINE_EXPIRES_MINUTE, TimeUnit.MINUTES);
        }
        return true;
    }

    @Override
    public void deleteToken(String account) {
        redisTemplate.delete(account);
    }
}
