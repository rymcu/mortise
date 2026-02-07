package com.rymcu.mortise.file.exception;

/**
 * 文件上传异常
 *
 * @author ronger
 */
public class FileUploadException extends FileException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
