package com.rymcu.mortise.core.exception;

import javax.security.auth.login.AccountException;

/**
 * Created on 2022/8/25 19:27.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 */
public class AccountExistsException extends AccountException {

    public AccountExistsException() {
    }

    public AccountExistsException(String message) {
        super(message);
    }
}
