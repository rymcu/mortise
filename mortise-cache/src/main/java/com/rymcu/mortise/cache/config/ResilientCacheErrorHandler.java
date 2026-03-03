package com.rymcu.mortise.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 弹性缓存错误处理器
 * <p>
 * 当缓存读取时遇到 {@link SerializationException}（通常由序列化格式不兼容导致，
 * 例如 Redis 中存有旧格式数据而当前配置要求携带 {@code @class} 类型标识符），
 * 将该异常视为缓存未命中并记录警告日志，由业务方法重新加载数据并以正确格式回写缓存。
 * <p>
 * 对缓存写入（put）和清除（evict/clear）的异常照常记录日志但不抛出，
 * 避免缓存层错误影响正常业务流程。
 *
 * @author ronger
 */
@Slf4j
public class ResilientCacheErrorHandler implements CacheErrorHandler {

    /**
     * 缓存读取失败时：若为序列化异常则视为缓存未命中（返回 null），否则记录错误日志。
     * 两种情况均不向上抛出异常，保证业务方法正常执行。
     */
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        if (exception instanceof SerializationException) {
            log.warn("缓存 [{}] key=[{}] 反序列化失败（可能为旧格式数据），将视为缓存未命中并重新加载: {}",
                    cache.getName(), key, exception.getMessage());
            // 主动驱逐旧数据，下次写入时以当前格式覆盖
            try {
                cache.evict(key);
            } catch (Exception evictEx) {
                log.warn("驱逐旧缓存条目失败 [{}] key=[{}]: {}", cache.getName(), key, evictEx.getMessage());
            }
        } else {
            log.error("缓存读取异常 [{}] key=[{}]", cache.getName(), key, exception);
        }
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("缓存写入异常 [{}] key=[{}]", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("缓存驱逐异常 [{}] key=[{}]", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("缓存清除异常 [{}]", cache.getName(), exception);
    }
}
