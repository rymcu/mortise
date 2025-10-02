package com.rymcu.mortise.cache.service;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供统一的缓存操作能力（基础设施层）
 * 
 * 注意：业务模块不应该直接使用此接口
 * 应该在业务模块中封装业务缓存服务（如 SystemCacheService）
 *
 * @author ronger
 */
public interface CacheService {

    // ==================== String 操作 ====================

    /**
     * 设置缓存
     */
    void set(String key, Object value);

    /**
     * 设置缓存（自动拼接 cacheName:key）
     *
     * @param cacheName 缓存区域名称（如 "user:info"）
     * @param key 缓存键（如用户ID）
     * @param value 缓存值
     */
    void set(String cacheName, String key, Object value);

    /**
     * 设置缓存并指定过期时间
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置缓存并指定过期时间（自动拼接 cacheName:key）
     *
     * @param cacheName 缓存区域名称（如 "user:info"）
     * @param key 缓存键（如用户ID）
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String cacheName, String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置缓存并指定过期时间
     */
    void set(String key, Object value, Duration timeout);

    /**
     * 设置缓存并指定过期时间（自动拼接 cacheName:key）
     *
     * @param cacheName 缓存区域名称（如 "user:info"）
     * @param key 缓存键（如用户ID）
     * @param value 缓存值
     * @param timeout 过期时间
     */
    void set(String cacheName, String key, Object value, Duration timeout);

    /**
     * 获取缓存
     */
    <T> T get(String key, Class<T> type);

    /**
     * 获取缓存（自动拼接 cacheName:key）
     *
     * @param cacheName 缓存区域名称（如 "user:info"）
     * @param key 缓存键（如用户ID）
     * @param type 返回值类型
     * @return 缓存值
     */
    <T> T get(String cacheName, String key, Class<T> type);

    /**
     * 删除缓存
     */
    Boolean delete(String key);

    /**
     * 删除缓存（自动拼接 cacheName:key）
     *
     * @param cacheName 缓存区域名称（如 "user:info"）
     * @param key 缓存键（如用户ID）
     * @return 是否删除成功
     */
    Boolean delete(String cacheName, String key);

    /**
     * 批量删除缓存
     */
    Long delete(Collection<String> keys);

    /**
     * 判断缓存是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取过期时间
     */
    Long getExpire(String key, TimeUnit unit);

    // ==================== Hash 操作 ====================

    /**
     * Hash设置
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * Hash获取
     */
    <T> T hGet(String key, String hashKey, Class<T> type);

    /**
     * Hash批量设置
     */
    void hSetAll(String key, Map<String, Object> map);

    /**
     * Hash获取所有
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * Hash删除
     */
    Long hDelete(String key, Object... hashKeys);

    /**
     * Hash是否存在
     */
    Boolean hHasKey(String key, String hashKey);

    // ==================== Set 操作 ====================

    /**
     * Set添加
     */
    Long sAdd(String key, Object... values);

    /**
     * Set获取所有成员
     */
    Set<Object> sMembers(String key);

    /**
     * Set是否包含
     */
    Boolean sIsMember(String key, Object value);

    /**
     * Set删除
     */
    Long sRemove(String key, Object... values);

    // ==================== List 操作 ====================

    /**
     * List右侧添加
     */
    Long lRightPush(String key, Object value);

    /**
     * List左侧添加
     */
    Long lLeftPush(String key, Object value);

    /**
     * List获取范围
     */
    <T> java.util.List<T> lRange(String key, long start, long end, Class<T> type);

    /**
     * List删除
     */
    Long lRemove(String key, long count, Object value);

    // ==================== 通用操作 ====================

    /**
     * 模糊匹配删除
     */
    void deletePattern(String pattern);

    /**
     * 获取所有匹配的key
     */
    Set<String> keys(String pattern);
}
