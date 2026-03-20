package com.rymcu.mortise.agent.model;

import java.util.List;
import java.util.Map;

/**
 * 聊天响应
 */
public record ChatResponse(
    String content,
    AgentIntent intent,
    ModelType modelType,
    String modelName,
    List<ToolCallRecord> toolCalls,
    TokenUsage tokenUsage,
    Map<String, Object> metadata
) {
    public static ChatResponse chat(String content, ModelType modelType, String modelName) {
        return new ChatResponse(content, AgentIntent.CHAT, modelType, modelName, null, null, null);
    }
    
    public static ChatResponse withToolCalls(
            String content,
            ModelType modelType,
            String modelName,
            List<ToolCallRecord> toolCalls
    ) {
        return new ChatResponse(content, AgentIntent.TOOL_CALL, modelType, modelName, toolCalls, null, null);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String content;
        private AgentIntent intent;
        private ModelType modelType;
        private String modelName;
        private List<ToolCallRecord> toolCalls;
        private TokenUsage tokenUsage;
        private Map<String, Object> metadata;
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder intent(AgentIntent intent) {
            this.intent = intent;
            return this;
        }
        
        public Builder modelType(ModelType modelType) {
            this.modelType = modelType;
            return this;
        }
        
        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }
        
        public Builder toolCalls(List<ToolCallRecord> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }
        
        public Builder tokenUsage(TokenUsage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public ChatResponse build() {
            return new ChatResponse(content, intent, modelType, modelName, toolCalls, tokenUsage, metadata);
        }
    }
}
