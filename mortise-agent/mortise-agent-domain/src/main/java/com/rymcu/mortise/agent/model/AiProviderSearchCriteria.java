package com.rymcu.mortise.agent.model;

/**
 * 提供商查询条件。
 */
public record AiProviderSearchCriteria(
        String name,
        String code,
        String query,
        Integer status
) {
}
