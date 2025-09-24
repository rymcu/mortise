package com.rymcu.mortise.config;

import com.rymcu.mortise.core.constant.CacheConstant;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;


/**
 * Created on 2021/10/9 9:25.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @packageName com.rymcu.mortise.config
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Resource
    private UserService userService;

    @Autowired
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对 redis 数据失效事件，进行数据处理
     * 适配统一缓存配置后的键格式
     *
     * @param message key
     * @param pattern pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取到失效的 key
        String expiredKey = message.toString();
        log.debug("检测到Redis键过期事件：{}", expiredKey);

        // 处理用户在线状态缓存过期
        if (isUserOnlineStatusKey(expiredKey)) {
            String account = extractAccountFromOnlineStatusKey(expiredKey);
            if (account != null && !account.isEmpty()) {
                log.info("用户在线状态缓存过期：{}, 用户账号：{}", expiredKey, account);
                try {
                    boolean flag = userService.updateLastOnlineTimeByAccount(account);
                    log.info("更新用户 {} 最后在线时间结果：{}", account, flag ? "成功" : "失败");
                } catch (Exception e) {
                    log.error("更新用户 {} 最后在线时间失败", account, e);
                }
            } else {
                log.warn("无法从过期键中提取用户账号：{}", expiredKey);
            }
        }

        // 处理其他缓存过期事件
        handleOtherCacheExpiration(expiredKey);

        super.onMessage(message, pattern);
    }

    /**
     * 检查是否为用户在线状态缓存键
     * 支持多种键格式：
     * 1. 旧格式：last_online:account
     * 2. 新格式：mortise:cache:userOnlineStatus:account
     */
    private boolean isUserOnlineStatusKey(String key) {
        return key.contains(CacheConstant.CACHE_NAME_PREFIX + CacheConstant.USER_ONLINE_STATUS_CACHE);
    }

    /**
     * 从在线状态缓存键中提取用户账号
     */
    private String extractAccountFromOnlineStatusKey(String key) {
        // 处理新格式：mortise:cache:userOnlineStatus:account
        String prefix = CacheConstant.CACHE_NAME_PREFIX + CacheConstant.USER_ONLINE_STATUS_CACHE + ":";
        if (key.startsWith(prefix)) {
            return key.substring(prefix.length());
        }

        return null;
    }

    /**
     * 处理其他类型的缓存过期事件
     * 可以在这里添加对其他缓存过期的处理逻辑
     */
    private void handleOtherCacheExpiration(String expiredKey) {
        // JWT Token 缓存过期处理
        if (expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + CacheConstant.JWT_TOKEN_CACHE)) {
            log.debug("JWT Token缓存过期：{}", expiredKey);
            // 可以在这里添加JWT Token过期的额外处理逻辑
        }

        // 用户会话缓存过期处理
        if (expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + CacheConstant.USER_SESSION_CACHE)) {
            log.debug("用户会话缓存过期：{}", expiredKey);
            // 可以在这里添加用户会话过期的额外处理逻辑
        }

        // 临时数据缓存过期处理
        if (expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + CacheConstant.TEMP_DATA_CACHE)) {
            log.debug("临时数据缓存过期：{}", expiredKey);
            // 可以在这里添加临时数据过期的清理逻辑
        }
    }
}
