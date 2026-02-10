package com.rymcu.mortise.web.aspect;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 限流切面
 * 基于 Resilience4j 实现限流功能
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
@ConditionalOnClass(RateLimiterRegistry.class)
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    /**
     * 使用构造函数注入一个 Optional&lt;RateLimiterRegistry&gt;
     * 这是处理可选依赖的最佳实践
     *
     * @param registryOptional Spring 会注入一个包含 RateLimiterRegistry 的 Optional,
     *                         如果该 bean 不存在，则注入 Optional.empty()
     */
    @Autowired
    public RateLimitAspect(Optional<RateLimiterRegistry> registryOptional) {
        // 如果 Optional 存在值，则获取它；否则，将字段设置为 null
        // 这样可以确保应用正常启动
        this.rateLimiterRegistry = registryOptional.orElse(null);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        if (rateLimiterRegistry == null) {
            log.warn("RateLimiterRegistry 未配置，限流功能不可用");
            return joinPoint.proceed();
        }

        String limiterName = rateLimit.name();
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterName);

        try {
            // 尝试获取许可
            return RateLimiter.decorateCheckedSupplier(rateLimiter, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }).get();  // Resilience4j API: 使用 get() 而非 apply()
        } catch (RequestNotPermitted e) {
            log.warn("限流触发: limiter={}, method={}",
                    limiterName, joinPoint.getSignature().toShortString());
            throw new BusinessException(rateLimit.message());
        }
    }
}
