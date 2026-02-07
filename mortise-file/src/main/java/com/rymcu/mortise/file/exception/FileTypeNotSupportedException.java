package com.rymcu.mortise.file.exception;

/**
 * 文件类型不支持异常
 *
 * @author ronger
 */
public class FileTypeNotSupportedException extends FileUploadException {

    private final String contentType;

    public FileTypeNotSupportedException(String contentType) {
        super(String.format("不支持的文件类型: %s", contentType));
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
