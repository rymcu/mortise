package com.rymcu.mortise.file.util;

import lombok.experimental.UtilityClass;

/**
 * 文件工具类
 *
 * @author ronger
 */
@UtilityClass
public class FileUtils {

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点）
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 根据文件大小返回可读的字符串
     *
     * @param size 文件大小（字节）
     * @return 可读的文件大小字符串
     */
    public static String formatFileSize(long size) {
        if (size < 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 检查是否为图片文件
     *
     * @param contentType MIME类型
     * @return 是否为图片
     */
    public static boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 检查是否为视频文件
     *
     * @param contentType MIME类型
     * @return 是否为视频
     */
    public static boolean isVideo(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

    /**
     * 检查是否为文档文件
     *
     * @param contentType MIME类型
     * @return 是否为文档
     */
    public static boolean isDocument(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.contains("application/pdf")
                || contentType.contains("application/msword")
                || contentType.contains("application/vnd.openxmlformats-officedocument")
                || contentType.contains("application/vnd.ms-excel")
                || contentType.contains("text/");
    }

    /**
     * 生成安全的文件名（移除特殊字符）
     *
     * @param filename 原始文件名
     * @return 安全的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        // 移除或替换不安全的字符
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
