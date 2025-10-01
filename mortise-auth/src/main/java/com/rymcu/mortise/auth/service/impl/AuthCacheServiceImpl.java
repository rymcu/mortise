package com.rymcu.mortise.auth.service.impl;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.cache.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/10/1 20:38.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.service.impl
 */
@Slf4j
@Service
public class AuthCacheServiceImpl implements AuthCacheService {

    @Resource
    private CacheService cacheService;

    @Override
    public void storeJwtToken(String account, String token) {
        cacheService.set(AuthCacheConstant.JWT_TOKEN_CACHE + account, token, JwtConstants.TOKEN_EXPIRES_MINUTE, TimeUnit.MINUTES);
        log.debug("存储 JWT Token：{} -> {}", account, token);
    }

    @Override
    public String getJwtToken(String account) {
        String token = cacheService.get(AuthCacheConstant.JWT_TOKEN_CACHE + account, String.class);
        log.debug("获取 JWT Token：{} -> {}", account, token != null ? "存在" : "不存在");
        return token;
    }

    @Override
    public Boolean removeJwtToken(String account) {
        Boolean deleted = cacheService.delete(AuthCacheConstant.JWT_TOKEN_CACHE + account);
        log.debug("删除 JWT Token：{}", account);
        return deleted;
    }

    // ==================== OAuth2 授权请求缓存操作实现 ====================

    @Override
    public void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest) {
        String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
        cacheService.set(cacheKey, authorizationRequest, 
                        AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES, 
                        TimeUnit.MINUTES);
        log.debug("存储 OAuth2 授权请求：{}", state);
    }

    @Override
    public <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz) {
        String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
        T request = cacheService.get(cacheKey, clazz);
        log.debug("获取 OAuth2 授权请求：{} -> {}", state, request != null ? "存在" : "不存在");
        return request;
    }

    @Override
    public void removeOAuth2AuthorizationRequest(String state) {
        String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
        cacheService.delete(cacheKey);
        log.debug("删除 OAuth2 授权请求：{}", state);
    }

}
