package com.rymcu.mortise.agent.model;

/**
 * 工具调用记录
 */
public record ToolCallRecord(
    String callId,
    String toolName,
    String arguments,
    String result,
    boolean success,
    String errorMessage
) {
    public static ToolCallRecord success(String callId, String toolName, String arguments, String result) {
        return new ToolCallRecord(callId, toolName, arguments, result, true, null);
    }
    
    public static ToolCallRecord failure(String callId, String toolName, String arguments, String errorMessage) {
        return new ToolCallRecord(callId, toolName, arguments, null, false, errorMessage);
    }
}
