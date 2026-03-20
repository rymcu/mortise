package com.rymcu.mortise.agent.model;

/**
 * Token 使用统计
 */
public record TokenUsage(
    long promptTokens,
    long completionTokens,
    long totalTokens
) {
    public static TokenUsage of(long promptTokens, long completionTokens) {
        return new TokenUsage(promptTokens, completionTokens, promptTokens + completionTokens);
    }
    
    public static TokenUsage empty() {
        return new TokenUsage(0, 0, 0);
    }
}
