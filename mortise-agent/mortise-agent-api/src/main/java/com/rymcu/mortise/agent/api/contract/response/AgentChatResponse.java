package com.rymcu.mortise.agent.api.contract.response;

import java.util.List;
import java.util.Map;

/**
 * Agent 聊天响应。
 */
public record AgentChatResponse(
        String conversationId,
        String content,
        String intent,
        String modelType,
        String modelName,
        List<ToolCallRecord> toolCalls,
        TokenUsage tokenUsage,
        Map<String, Object> metadata
) {
}
