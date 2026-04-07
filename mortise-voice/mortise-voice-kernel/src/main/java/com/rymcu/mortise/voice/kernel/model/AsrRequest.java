package com.rymcu.mortise.voice.kernel.model;

/**
 * 一次性识别请求。
 */
public record AsrRequest(
        String profileCode,
        String fileName,
        String contentType,
        long size,
        byte[] content
) {
}