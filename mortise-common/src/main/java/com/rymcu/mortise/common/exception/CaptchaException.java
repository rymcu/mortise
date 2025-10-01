package com.rymcu.mortise.common.exception;

/**
 * 验证码异常
 * 
 * @author ronger
 */
public class CaptchaException extends IllegalArgumentException {

    public CaptchaException() {
        super("验证码错误");
    }

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}