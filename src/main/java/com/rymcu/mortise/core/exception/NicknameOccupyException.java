package com.rymcu.mortise.core.exception;

/**
 * Created on 2022/8/25 19:11.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 */
public class NicknameOccupyException extends BusinessException {

    public NicknameOccupyException() {
    }

    public NicknameOccupyException(String message) {
        super(message);
    }

    public NicknameOccupyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NicknameOccupyException(Throwable cause) {
        super(cause);
    }

    public NicknameOccupyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
