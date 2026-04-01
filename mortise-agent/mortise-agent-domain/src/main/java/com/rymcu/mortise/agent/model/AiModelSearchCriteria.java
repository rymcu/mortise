package com.rymcu.mortise.agent.model;

/**
 * 模型查询条件。
 */
public record AiModelSearchCriteria(
        Long providerId,
        String modelName,
        String query,
        Integer status
) {
}
