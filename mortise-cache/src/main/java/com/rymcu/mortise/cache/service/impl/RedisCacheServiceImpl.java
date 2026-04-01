package com.rymcu.mortise.cache.service.impl;

import com.rymcu.mortise.cache.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Redis 缓存服务实现
 *
 * @author ronger
 */
@Slf4j
@Service
public class RedisCacheServiceImpl implements CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存键分隔符
     */
    private static final String KEY_SEPARATOR = ":";

    private static final DefaultRedisScript<Long> INCREMENT_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>(
            """
                    local current = redis.call('INCRBY', KEYS[1], ARGV[1])
                    if current == tonumber(ARGV[1]) then
                        redis.call('PEXPIRE', KEYS[1], ARGV[2])
                    end
                    return current
                    """,
            Long.class
    );

    /**
     * 构建完整的缓存键
     *
     * @param cacheName 缓存区域名称
     * @param key 缓存键
     * @return 完整的缓存键（cacheName:key）
     */
    private String buildKey(String cacheName, String key) {
        return cacheName + KEY_SEPARATOR + key;
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String cacheName, String key, Object value) {
        set(buildKey(cacheName, key), value);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public void set(String cacheName, String key, Object value, long timeout, TimeUnit unit) {
        set(buildKey(cacheName, key), value, timeout, unit);
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    @Override
    public void set(String cacheName, String key, Object value, Duration timeout) {
        set(buildKey(cacheName, key), value, timeout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = readWithSerializationGuard(key, () -> redisTemplate.opsForValue().get(key), null);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.warn("类型转换失败: key={}, expectedType={}, actualType={}", 
                    key, type.getName(), value.getClass().getName());
            return null;
        }
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> type) {
        return get(buildKey(cacheName, key), type);
    }

    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Boolean delete(String cacheName, String key) {
        return delete(buildKey(cacheName, key));
    }

    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    @Override
    public Long increment(String key) {
        return increment(key, 1L);
    }

    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long increment(String key, long delta, Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        return redisTemplate.execute(
                INCREMENT_WITH_EXPIRE_SCRIPT,
                Collections.singletonList(key),
                delta,
                timeout.toMillis()
        );
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey, Class<T> type) {
        Object value = readWithSerializationGuard(key,
                () -> redisTemplate.opsForHash().get(key, hashKey), null);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.warn("Hash类型转换失败: key={}, hashKey={}, expectedType={}", 
                    key, hashKey, type.getName());
            return null;
        }
    }

    @Override
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        return readWithSerializationGuard(key, () -> redisTemplate.opsForHash().entries(key), Collections.emptyMap());
    }

    @Override
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    @Override
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Set<Object> sMembers(String key) {
        return readWithSerializationGuard(key, () -> redisTemplate.opsForSet().members(key), Collections.emptySet());
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end, Class<T> type) {
        List<Object> range = readWithSerializationGuard(key,
                () -> redisTemplate.opsForList().range(key, start, end), Collections.emptyList());
        if (range == null || range.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return range.stream()
                    .map(obj -> (T) obj)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
            log.warn("List类型转换失败: key={}, expectedType={}", key, type.getName());
            return Collections.emptyList();
        }
    }

    @Override
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    @Override
    public void deletePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 兼容 Redis 中遗留的旧格式 JSON（缺少 @class 类型标识）。
     * 对于直接走 RedisTemplate 的读取链路，异常不会进入 Spring CacheErrorHandler，
     * 因此在此处统一兜底：视为缓存未命中并主动驱逐坏数据。
     */
    private <T> T readWithSerializationGuard(String key, Supplier<T> reader, T fallbackValue) {
        try {
            return reader.get();
        } catch (RuntimeException exception) {
            if (isSerializationFailure(exception)) {
                log.warn("Redis 缓存反序列化失败，已按未命中处理并驱逐旧数据: key={}, message={}",
                        key, exception.getMessage());
                deleteCorruptedKey(key);
                return fallbackValue;
            }
            throw exception;
        }
    }

    private boolean isSerializationFailure(RuntimeException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SerializationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void deleteCorruptedKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException deleteException) {
            log.warn("驱逐损坏 Redis 缓存失败: key={}, message={}", key, deleteException.getMessage());
        }
    }
}
