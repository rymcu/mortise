package com.rymcu.mortise.agent.application.query;

/**
 * AI 模型查询条件。
 */
public record AiModelSearchQuery(
        Long providerId,
        String modelName,
        String query,
        Integer status
) {
}
