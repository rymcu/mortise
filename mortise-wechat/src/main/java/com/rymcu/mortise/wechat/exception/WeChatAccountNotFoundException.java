package com.rymcu.mortise.wechat.exception;

import com.rymcu.mortise.common.exception.BusinessException;

/**
 * 微信账号未找到异常
 * <p>当根据账号ID无法找到对应的微信配置时抛出此异常</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public class WeChatAccountNotFoundException extends BusinessException {

    public WeChatAccountNotFoundException(String message) {
        super(message);
    }

    public WeChatAccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeChatAccountNotFoundException(Long accountId) {
        super("No WeChat MP configuration found for accountId: " + accountId);
    }

    public WeChatAccountNotFoundException(Long accountId, Throwable cause) {
        super("No WeChat MP configuration found for accountId: " + accountId, cause);
    }
}