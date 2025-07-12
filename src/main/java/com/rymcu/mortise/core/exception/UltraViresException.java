package com.rymcu.mortise.core.exception;

/**
 * @author KKould
 */
public class UltraViresException extends BusinessException {

    public UltraViresException() {
        super();
    }

    public UltraViresException(String message) {
        super(message);
    }

    public UltraViresException(String message, Throwable cause) {
        super(message, cause);
    }

    public UltraViresException(Throwable cause) {
        super(cause);
    }

    protected UltraViresException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
