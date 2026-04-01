package com.rymcu.mortise.web.aspect;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.auth.util.CurrentUserUtils;
import com.rymcu.mortise.web.annotation.RateLimit;
import com.rymcu.mortise.web.rate.DistributedRateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 限流切面
 * 基于 Redis 实现分布式限流。
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private final DistributedRateLimitService distributedRateLimitService;
    private final HttpServletRequest request;

    /**
     * 使用 Optional 兼容未接入缓存的场景。
     */
    @Autowired
    public RateLimitAspect(Optional<DistributedRateLimitService> distributedRateLimitService,
                           HttpServletRequest request) {
        this.distributedRateLimitService = distributedRateLimitService.orElse(null);
        this.request = request;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        if (distributedRateLimitService == null) {
            log.warn("DistributedRateLimitService 未配置，跳过限流");
            return joinPoint.proceed();
        }

        String rateLimitKey = buildRateLimitKey(joinPoint, rateLimit);
        boolean permitted = distributedRateLimitService.tryAcquire(
                rateLimitKey,
                rateLimit.limitForPeriod(),
                rateLimit.refreshPeriodSeconds()
        );
        if (!permitted) {
            log.warn("限流触发: key={}, method={}", rateLimitKey, joinPoint.getSignature().toShortString());
            throw new BusinessException(rateLimit.message());
        }
        return joinPoint.proceed();
    }

    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String subjectKey = switch (rateLimit.keyType()) {
            case IP -> resolveClientIp();
            case METHOD -> joinPoint.getSignature().toShortString();
            case IP_AND_METHOD -> resolveClientIp() + ":" + joinPoint.getSignature().toShortString();
            case USER_ID -> String.valueOf(CurrentUserUtils.getUserIdOrDefault(-1L));
            case IP_AND_USER_ID -> resolveClientIp() + ":" + CurrentUserUtils.getUserIdOrDefault(-1L);
            case CUSTOM -> evaluateCustomKey(joinPoint, rateLimit.keyExpression());
        };
        return rateLimit.name() + ":" + subjectKey;
    }

    private String evaluateCustomKey(ProceedingJoinPoint joinPoint, String expression) {
        if (!StringUtils.hasText(expression)) {
            return joinPoint.getSignature().toShortString();
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        Object value = SPEL_PARSER.parseExpression(expression).getValue(context);
        return value == null ? "null" : value.toString();
    }

    private String resolveClientIp() {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
