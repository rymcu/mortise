package com.rymcu.mortise.core.result;

import lombok.Data;

/**
 * @author ronger
 */
@Data
public class GlobalResult<T> {
    private T data;
    private int code;
    private String message;

    public GlobalResult() {
    }

    public GlobalResult(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

}
