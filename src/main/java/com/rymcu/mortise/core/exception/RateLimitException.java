package com.rymcu.mortise.core.exception;

/**
 * 限流异常
 * 当API请求超过限制时抛出此异常
 *
 * @author ronger
 */
public class RateLimitException extends RuntimeException {
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
