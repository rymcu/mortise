package com.rymcu.mortise.agent.application.query;

/**
 * AI 提供商查询条件。
 */
public record AiProviderSearchQuery(
        String name,
        String code,
        String query,
        Integer status
) {
}
