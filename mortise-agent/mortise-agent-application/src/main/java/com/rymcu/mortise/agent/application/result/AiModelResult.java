package com.rymcu.mortise.agent.application.result;

import java.time.LocalDateTime;

/**
 * AI 模型结果。
 */
public record AiModelResult(
        Long id,
        Long providerId,
        String providerName,
        String modelName,
        String displayName,
        Integer status,
        Integer sortNo,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
