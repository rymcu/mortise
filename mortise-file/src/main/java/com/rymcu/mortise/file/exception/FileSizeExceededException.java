package com.rymcu.mortise.file.exception;

/**
 * 文件大小超出限制异常
 *
 * @author ronger
 */
public class FileSizeExceededException extends FileUploadException {

    private final long maxSize;
    private final long actualSize;

    public FileSizeExceededException(long maxSize, long actualSize) {
        super(String.format("文件大小超出限制: 最大允许 %d bytes, 实际 %d bytes", maxSize, actualSize));
        this.maxSize = maxSize;
        this.actualSize = actualSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getActualSize() {
        return actualSize;
    }
}
