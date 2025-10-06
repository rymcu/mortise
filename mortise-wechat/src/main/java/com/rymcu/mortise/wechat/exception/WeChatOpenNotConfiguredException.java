package com.rymcu.mortise.wechat.exception;

/**
 * Created on 2025/10/6 22:01.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.wechat.exception
 */
public class WeChatOpenNotConfiguredException extends IllegalStateException {
    public WeChatOpenNotConfiguredException(String message) {
        super(message);
    }
}
