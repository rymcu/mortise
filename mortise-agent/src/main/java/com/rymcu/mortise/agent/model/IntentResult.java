package com.rymcu.mortise.agent.model;

import java.util.List;

/**
 * 意图识别结果
 */
public record IntentResult(
    AgentIntent intent,
    double confidence,
    String reasoning,
    List<String> recommendedTools
) {
    public boolean isToolCall() {
        return AgentIntent.TOOL_CALL.equals(intent);
    }
    
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    public static IntentResult chat(double confidence, String reasoning) {
        return new IntentResult(AgentIntent.CHAT, confidence, reasoning, null);
    }
    
    public static IntentResult toolCall(double confidence, String reasoning, List<String> tools) {
        return new IntentResult(AgentIntent.TOOL_CALL, confidence, reasoning, tools);
    }
}
