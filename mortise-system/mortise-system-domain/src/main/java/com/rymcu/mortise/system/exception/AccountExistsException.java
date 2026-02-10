package com.rymcu.mortise.system.exception;

import javax.security.auth.login.AccountException;

/**
 * 账户已存在异常
 * <p>
 * 用于注册流程中检测到账户（邮箱）已存在的情况
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
