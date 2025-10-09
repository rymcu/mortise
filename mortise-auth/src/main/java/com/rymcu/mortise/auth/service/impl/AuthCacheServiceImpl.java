package com.rymcu.mortise.auth.service.impl;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.cache.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

    @Resource
    private CacheManager cacheManager;

    @Override
    public void storeJwtToken(String account, String token) {
        cacheService.set(AuthCacheConstant.JWT_TOKEN_CACHE, account, token, JwtConstants.TOKEN_EXPIRES_MINUTE, TimeUnit.MINUTES);
        log.debug("存储 JWT Token：{} -> {}", account, token);
    }

    @Override
    public String getJwtToken(String account) {
        String token = cacheService.get(AuthCacheConstant.JWT_TOKEN_CACHE, account, String.class);
        log.debug("获取 JWT Token：{} -> {}", account, token != null ? "存在" : "不存在");
        return token;
    }

    @Override
    public Boolean removeJwtToken(String account) {
        Boolean deleted = cacheService.delete(AuthCacheConstant.JWT_TOKEN_CACHE, account);
        log.debug("删除 JWT Token：{}", account);
        return deleted;
    }

    // ==================== OAuth2 授权请求缓存操作实现 ====================

    @Override
    public void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
        if (cache != null) {
            cache.put(state, authorizationRequest);
            log.info("存储 OAuth2 授权请求：state={}, type={}", state,
                    authorizationRequest != null ? authorizationRequest.getClass().getSimpleName() : "null");
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(state);
            if (wrapper != null) {
                Object value = wrapper.get();
                String className = value != null ? value.getClass().getName() : "null";
                log.info("获取 OAuth2 授权请求：state={} -> 找到，类型={}", state,
                        className);
                try {
                    return (T) value;
                } catch (ClassCastException e) {
                    log.error("OAuth2 授权请求类型转换失败：state={}, expectedType={}, actualType={}",
                            state, clazz.getName(), className, e);
                    return null;
                }
            } else {
                log.warn("获取 OAuth2 授权请求：state={} -> 未找到", state);
                return null;
            }
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
            return null;
        }
    }

    @Override
    public void removeOAuth2AuthorizationRequest(String state) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
        if (cache != null) {
            cache.evict(state);
            log.debug("删除 OAuth2 授权请求：state={}", state);
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE);
        }
    }

    @Override
    public void storeOAuth2ParameterMap(String state, Object parameterMap) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
        if (cache != null) {
            cache.put(state, parameterMap);
            log.info("存储 OAuth2 参数对象：state={}, type={}", state,
                    parameterMap != null ? parameterMap.getClass().getSimpleName() : "null");
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOAuth2ParameterMap(String state, Class<T> clazz) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(state);
            if (wrapper != null) {
                Object value = wrapper.get();
                String className = value != null ? value.getClass().getName() : "null";
                log.info("获取 OAuth2 参数对象：state={} -> 找到，类型={}", state,
                        className);
                try {
                    return (T) value;
                } catch (ClassCastException e) {
                    log.error("OAuth2 参数对象类型转换失败：state={}, expectedType={}, actualType={}",
                            state, clazz.getName(), className, e);
                    return null;
                }
            } else {
                log.warn("获取 OAuth2 参数对象：state={} -> 未找到", state);
                return null;
            }
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
            return null;
        }
    }

    @Override
    public void removeOAuth2ParameterMap(String state) {
        Cache cache = cacheManager.getCache(AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
        if (cache != null) {
            cache.evict(state);
            log.debug("删除 OAuth2 参数对象：state={}", state);
        } else {
            log.error("未找到缓存：{}", AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE);
        }

    }

}
