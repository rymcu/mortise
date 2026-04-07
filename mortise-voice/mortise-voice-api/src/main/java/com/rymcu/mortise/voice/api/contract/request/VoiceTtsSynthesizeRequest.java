package com.rymcu.mortise.voice.api.contract.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户端同步语音合成请求。
 */
public record VoiceTtsSynthesizeRequest(
        @NotBlank(message = "语音配置编码不能为空") String profileCode,
        @NotBlank(message = "待合成文本不能为空") String text,
        String voiceName,
        String sourceModule
) {
}