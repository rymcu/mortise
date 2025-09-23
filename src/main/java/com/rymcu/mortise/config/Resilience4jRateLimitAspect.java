package com.rymcu.mortise.config;

import com.rymcu.mortise.annotation.Resilience4jRateLimit;
import com.rymcu.mortise.core.exception.RateLimitException;
import com.rymcu.mortise.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Resilience4j限流切面
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
public class Resilience4jRateLimitAspect {

    private final Resilience4jRateLimiter rateLimiter;
    private final ExpressionParser parser = new SpelExpressionParser();

    public Resilience4jRateLimitAspect(Resilience4jRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Around("@annotation(resilience4jRateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, Resilience4jRateLimit resilience4jRateLimit) throws Throwable {
        String key = generateKey(joinPoint, resilience4jRateLimit);

        log.debug("Resilience4j限流检查开始: key={}, limitForPeriod={}, refreshPeriod={}s",
                key, resilience4jRateLimit.limitForPeriod(), resilience4jRateLimit.refreshPeriodSeconds());

        try {
            // 如果启用降级，使用装饰器模式
            if (resilience4jRateLimit.enableFallback() && StringUtils.hasText(resilience4jRateLimit.fallbackMethod())) {
                return rateLimiter.executeWithRateLimit(
                        key,
                        resilience4jRateLimit.limitForPeriod(),
                        resilience4jRateLimit.refreshPeriodSeconds(),
                        resilience4jRateLimit.timeoutMillis(),
                        () -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throw new RuntimeException(throwable);
                            }
                        },
                        resilience4jRateLimit.message()
                );
            } else {
                // 普通限流检查
                rateLimiter.checkRateLimit(
                        key,
                        resilience4jRateLimit.limitForPeriod(),
                        resilience4jRateLimit.refreshPeriodSeconds(),
                        resilience4jRateLimit.timeoutMillis(),
                        resilience4jRateLimit.message()
                );
                return joinPoint.proceed();
            }
        } catch (RateLimitException e) {
            // 如果启用降级，尝试调用降级方法
            if (resilience4jRateLimit.enableFallback() && StringUtils.hasText(resilience4jRateLimit.fallbackMethod())) {
                return invokeFallbackMethod(joinPoint, resilience4jRateLimit.fallbackMethod(), e);
            }
            throw e;
        }
    }

    /**
     * 生成限流key
     */
    private String generateKey(ProceedingJoinPoint joinPoint, Resilience4jRateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 如果指定了名称，直接使用
        if (StringUtils.hasText(rateLimit.name())) {
            return rateLimit.name();
        }

        StringBuilder keyBuilder = new StringBuilder();

        switch (rateLimit.keyType()) {
            case IP:
                keyBuilder.append(getClientIp());
                break;
            case METHOD:
                keyBuilder.append(className).append(".").append(methodName);
                break;
            case USER_ID:
                keyBuilder.append(getCurrentUserId());
                break;
            case IP_AND_USER_ID:
                keyBuilder.append(getClientIp()).append(":").append(getCurrentUserId());
                break;
            case CUSTOM:
                if (StringUtils.hasText(rateLimit.keyExpression())) {
                    keyBuilder.append(evaluateSpelExpression(rateLimit.keyExpression(), joinPoint));
                } else {
                    log.warn("CUSTOM keyType指定但keyExpression为空，使用默认IP_AND_METHOD策略");
                    keyBuilder.append(getClientIp()).append(":").append(className).append(".").append(methodName);
                }
                break;
            default:
                keyBuilder.append(getClientIp()).append(":").append(className).append(".").append(methodName);
        }

        return "resilience4j:rate_limit:" + keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return Utils.getIpAddress();
        }
        return "unknown";
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败", e);
        }
        return "anonymous";
    }

    /**
     * 执行SpEL表达式
     */
    private String evaluateSpelExpression(String expression, ProceedingJoinPoint joinPoint) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 设置方法参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            // 设置常用变量
            context.setVariable("ip", getClientIp());
            context.setVariable("userId", getCurrentUserId());
            context.setVariable("method", signature.getMethod().getName());
            context.setVariable("class", signature.getMethod().getDeclaringClass().getSimpleName());

            return parser.parseExpression(expression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("SpEL表达式执行失败: {}", expression, e);
            return "spel_error";
        }
    }

    /**
     * 调用降级方法
     */
    private Object invokeFallbackMethod(ProceedingJoinPoint joinPoint, String fallbackMethodName, Exception originalException) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method originalMethod = signature.getMethod();
            Class<?> targetClass = joinPoint.getTarget().getClass();

            // 查找降级方法
            Method fallbackMethod = targetClass.getDeclaredMethod(fallbackMethodName, originalMethod.getParameterTypes());
            fallbackMethod.setAccessible(true);

            log.info("执行降级方法: {}.{}", targetClass.getSimpleName(), fallbackMethodName);
            return fallbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());

        } catch (Exception e) {
            log.error("执行降级方法失败: {}", fallbackMethodName, e);
            // 降级失败，抛出原始异常
            throw new RateLimitException("限流触发且降级方法执行失败", originalException);
        }
    }
}
