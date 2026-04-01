package com.rymcu.mortise.agent.api.contract.response;

/**
 * 工具调用记录响应。
 */
public record ToolCallRecord(
        String callId,
        String toolName,
        String arguments,
        String result,
        boolean success,
        String errorMessage
) {
}
