package com.rymcu.mortise.agent.application.result;

import java.time.LocalDateTime;

/**
 * AI 提供商结果。
 */
public record AiProviderResult(
        Long id,
        String name,
        String code,
        String apiKey,
        String baseUrl,
        String defaultModelName,
        Integer status,
        Integer sortNo,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
