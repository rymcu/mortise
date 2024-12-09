package com.rymcu.mortise.core.exception;

/**
 * 验证码错误异常类
 *
 * @author ronger
 */
public class CaptchaException extends IllegalArgumentException {

    public CaptchaException() {
        super("验证码错误");
    }

}
