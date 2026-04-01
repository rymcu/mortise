package com.rymcu.mortise.cache.service.impl;

import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.cache.service.CacheVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基于 Redis 的缓存版本服务。
 */
@Service
@RequiredArgsConstructor
public class RedisCacheVersionService implements CacheVersionService {

    private static final String VERSION_KEY_PREFIX = "cache:version:";

    private final CacheService cacheService;

    @Override
    public long currentVersion(String namespace) {
        Long version = cacheService.get(buildKey(namespace), Long.class);
        return version == null ? 0L : version;
    }

    @Override
    public long bumpVersion(String namespace) {
        Long version = cacheService.increment(buildKey(namespace));
        return version == null ? 0L : version;
    }

    private String buildKey(String namespace) {
        return VERSION_KEY_PREFIX + namespace;
    }
}
