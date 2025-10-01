package com.rymcu.mortise.cache.spi;

/**
 * 缓存失效处理扩展接口 (SPI)
 * 业务模块可实现此接口来处理自己的缓存失效事件
 * 
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class UserCacheExpirationHandler implements CacheExpirationHandler {
 *     &#64;Override
 *     public boolean supports(String expiredKey) {
 *         return expiredKey.contains("userOnlineStatus");
 *     }
 *     
 *     &#64;Override
 *     public void handle(String expiredKey) {
 *         String account = extractAccount(expiredKey);
 *         userService.updateLastOnlineTime(account);
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface CacheExpirationHandler {

    /**
     * 获取处理器优先级，数字越小优先级越高
     * 默认优先级为 100
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 判断是否支持处理该失效键
     * 
     * @param expiredKey 失效的缓存键
     * @return true 如果支持处理，false 否则
     */
    boolean supports(String expiredKey);

    /**
     * 处理缓存失效事件
     * 
     * @param expiredKey 失效的缓存键
     */
    void handle(String expiredKey);

    /**
     * 处理器是否启用
     * 默认启用
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 获取处理器名称，用于日志记录
     * 默认返回类名
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}