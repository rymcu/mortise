package com.rymcu.mortise.agent.api.contract.response;

/**
 * Token 使用统计响应。
 */
public record TokenUsage(
        long promptTokens,
        long completionTokens,
        long totalTokens
) {
}
