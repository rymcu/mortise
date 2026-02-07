package com.rymcu.mortise.file.exception;

/**
 * 文件异常基类
 *
 * @author ronger
 */
public class FileException extends RuntimeException {

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}
