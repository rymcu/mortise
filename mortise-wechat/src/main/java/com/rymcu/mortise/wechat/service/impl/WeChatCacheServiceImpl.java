package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.wechat.constant.WeChatCacheConstant;
import com.rymcu.mortise.wechat.service.WeChatCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 微信缓存服务实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(CacheService.class)
public class WeChatCacheServiceImpl implements WeChatCacheService {


    private final CacheService cacheService;

    @Override
    public void cacheAuthState(String state, Long accountId) {
        String value = accountId != null ? accountId.toString() : "default";

        cacheService.set(WeChatCacheConstant.STATE, state, value, WeChatCacheConstant.STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("缓存授权 State - state: {}, accountId: {}", state, accountId);
    }

    @Override
    public Long validateAndGetAccountId(String state) {
        if (state == null || state.isEmpty()) {
            throw new IllegalStateException("State 参数不能为空");
        }

        String cachedValue = cacheService.get(WeChatCacheConstant.STATE, state, String.class);

        if (cachedValue == null) {
            throw new IllegalStateException("Invalid or expired state parameter");
        }

        // 删除已使用的 state（防止重放攻击）
        cacheService.delete(WeChatCacheConstant.STATE, state);

        log.debug("验证 State 成功 - state: {}, accountId: {}", state, cachedValue);

        // 返回 accountId
        if ("default".equals(cachedValue)) {
            return null; // null 表示使用默认账号
        }

        try {
            return Long.parseLong(cachedValue);
        } catch (NumberFormatException e) {
            log.error("无效的缓存 accountId: {}", cachedValue);
            return null;
        }
    }

    @Override
    public void cacheUserInfo(String openId, Object userInfo, long minutes) {
        cacheService.set(WeChatCacheConstant.USER_INFO, openId, userInfo, minutes, TimeUnit.MINUTES);

        log.debug("缓存用户信息 - openId: {}, minutes: {}", openId, minutes);
    }

    @Override
    public <T> T getUserInfo(String openId, Class<T> type) {
        return cacheService.get(WeChatCacheConstant.USER_INFO, openId, type);
    }

    @Override
    public void deleteUserInfo(String openId) {
        cacheService.delete(WeChatCacheConstant.USER_INFO, openId);

        log.debug("删除用户信息缓存 - openId: {}", openId);
    }

    @Override
    public void cacheAccessToken(Long accountId, String accessToken, int expiresIn) {
        String key = (accountId != null ? String.valueOf(accountId) : "default");

        // 提前 5 分钟过期，避免边界问题
        long expireSeconds = Math.max(expiresIn - 300, 60);

        cacheService.set(WeChatCacheConstant.ACCESS_TOKEN, key, accessToken, expireSeconds, TimeUnit.SECONDS);

        log.debug("缓存访问令牌 - accountId: {}, expiresIn: {}s", accountId, expireSeconds);
    }

    @Override
    public String getAccessToken(Long accountId) {
        String key = (accountId != null ? String.valueOf(accountId) : "default");
        return cacheService.get(WeChatCacheConstant.ACCESS_TOKEN, key, String.class);
    }

    @Override
    public void deleteAccessToken(Long accountId) {
        String key = (accountId != null ? String.valueOf(accountId) : "default");
        cacheService.delete(WeChatCacheConstant.ACCESS_TOKEN, key);

        log.debug("删除访问令牌缓存 - accountId: {}", accountId);
    }
}
