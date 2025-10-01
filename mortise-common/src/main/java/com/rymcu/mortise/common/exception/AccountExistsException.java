package com.rymcu.mortise.common.exception;

import javax.security.auth.login.AccountException;

/**
 * 账号已存在异常
 * 当尝试注册已存在的账号时抛出此异常
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2022/8/25
 */
public class AccountExistsException extends AccountException {

    public AccountExistsException() {
        super();
    }

    public AccountExistsException(String message) {
        super(message);
    }
}
