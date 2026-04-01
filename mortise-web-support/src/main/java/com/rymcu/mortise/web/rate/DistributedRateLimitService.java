package com.rymcu.mortise.web.rate;

import com.rymcu.mortise.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 基于 Redis 的分布式限流服务。
 */
@Service
@RequiredArgsConstructor
public class DistributedRateLimitService {

    private static final String KEY_PREFIX = "rate-limit:";

    private final CacheService cacheService;

    public boolean tryAcquire(String key, int limitForPeriod, long refreshPeriodSeconds) {
        Long current = cacheService.increment(
                KEY_PREFIX + key,
                1L,
                Duration.ofSeconds(Math.max(refreshPeriodSeconds, 1L))
        );
        return current != null && current <= limitForPeriod;
    }
}
