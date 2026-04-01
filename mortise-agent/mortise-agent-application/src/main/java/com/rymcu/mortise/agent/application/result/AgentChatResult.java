package com.rymcu.mortise.agent.application.result;

import com.rymcu.mortise.agent.kernel.model.ChatResponse;

import java.util.List;
import java.util.Map;

/**
 * Agent 聊天结果。
 */
public record AgentChatResult(
        String conversationId,
        String content,
        String intent,
        String modelType,
        String modelName,
        List<ToolCallRecordResult> toolCalls,
        TokenUsageResult tokenUsage,
        Map<String, Object> metadata
) {

    public static AgentChatResult from(ChatResponse response) {
        return new AgentChatResult(
                response.conversationId(),
                response.content(),
                response.intent() == null ? null : response.intent().name(),
                response.modelType() == null ? null : response.modelType().name(),
                response.modelName(),
                response.toolCalls() == null ? null : response.toolCalls().stream()
                        .map(toolCall -> new ToolCallRecordResult(
                                toolCall.callId(),
                                toolCall.toolName(),
                                toolCall.arguments(),
                                toolCall.result(),
                                toolCall.success(),
                                toolCall.errorMessage()
                        ))
                        .toList(),
                response.tokenUsage() == null ? null : new TokenUsageResult(
                        response.tokenUsage().promptTokens(),
                        response.tokenUsage().completionTokens(),
                        response.tokenUsage().totalTokens()
                ),
                response.metadata()
        );
    }

    public AgentChatResult withConversationId(String conversationId) {
        return new AgentChatResult(
                conversationId,
                content,
                intent,
                modelType,
                modelName,
                toolCalls,
                tokenUsage,
                metadata
        );
    }

    public record ToolCallRecordResult(
            String callId,
            String toolName,
            String arguments,
            String result,
            boolean success,
            String errorMessage
    ) {
    }

    public record TokenUsageResult(
            long promptTokens,
            long completionTokens,
            long totalTokens
    ) {
    }
}
