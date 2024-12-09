package com.rymcu.mortise.enumerate;

import lombok.Getter;

/**
 * @author ronger
 */
@Getter
public enum TransactionCode {

    INSUFFICIENT_BALANCE(901, "余额不足"),
    UNKNOWN_ACCOUNT(902, "账号不存在"),
    FAIL(903, "交易失败");

    private final int code;

    private final String message;

    TransactionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
