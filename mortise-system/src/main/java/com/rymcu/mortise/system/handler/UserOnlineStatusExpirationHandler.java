package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.cache.constant.CacheConstant;
import com.rymcu.mortise.cache.spi.CacheExpirationHandler;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户在线状态缓存失效处理器
 * 处理用户在线状态缓存过期事件，更新用户最后在线时间
 * 
 * @author ronger
 */
@Slf4j
@Component
public class UserOnlineStatusExpirationHandler implements CacheExpirationHandler {

    @Autowired
    private UserService userService;

    @Override
    public int getOrder() {
        return 10; // 高优先级
    }

    @Override
    public boolean supports(String expiredKey) {
        // 检查是否为用户在线状态缓存键
        return expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + SystemCacheConstant.USER_ONLINE_STATUS_CACHE + ":");
    }

    @Override
    public void handle(String expiredKey) {
        String account = extractAccountFromKey(expiredKey);
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

    @Override
    public String getName() {
        return "用户在线状态缓存失效处理器";
    }

    /**
     * 从缓存键中提取用户账号
     * 
     * @param key 缓存键，格式如：mortise:userOnlineStatus:account
     * @return 用户账号，如果提取失败则返回 null
     */
    private String extractAccountFromKey(String key) {
        String prefix = CacheConstant.CACHE_NAME_PREFIX + SystemCacheConstant.USER_ONLINE_STATUS_CACHE + ":";
        if (key.startsWith(prefix)) {
            return key.substring(prefix.length());
        }
        return null;
    }
}