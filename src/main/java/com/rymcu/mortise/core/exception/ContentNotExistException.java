package com.rymcu.mortise.core.exception;

/**
 * @author KKould
 */
public class ContentNotExistException extends BusinessException {

    public ContentNotExistException() {
    }

    public ContentNotExistException(String message) {
        super(message);
    }

    public ContentNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentNotExistException(Throwable cause) {
        super(cause);
    }

    public ContentNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
