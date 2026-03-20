package com.rymcu.mortise.agent.model;

/**
 * Agent 意图枚举
 * 定义两种主要意图类型：普通聊天和工具调用
 */
public enum AgentIntent {
    
    /**
     * 普通聊天意图 - 直接回复，无需工具调用
     */
    CHAT("chat", "普通对话"),
    
    /**
     * 工具调用意图 - 需要调用工具来完成任务
     */
    TOOL_CALL("tool_call", "工具调用");
    
    private final String code;
    private final String description;
    
    AgentIntent(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static AgentIntent fromCode(String code) {
        for (AgentIntent intent : values()) {
            if (intent.code.equals(code)) {
                return intent;
            }
        }
        return CHAT;
    }
}
