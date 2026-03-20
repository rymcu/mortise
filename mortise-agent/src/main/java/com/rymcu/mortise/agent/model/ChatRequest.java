package com.rymcu.mortise.agent.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求
 */
public record ChatRequest(
    @NotBlank(message = "消息内容不能为空")
    String message,
    String conversationId,
    ModelType modelType,
    String modelName,
    List<ChatMessage> history,
    Map<String, Object> metadata
) {
    public static ChatRequest of(String message) {
        return new ChatRequest(message, null, null, null, null, null);
    }
    
    public static ChatRequest of(String message, String conversationId) {
        return new ChatRequest(message, conversationId, null, null, null, null);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String message;
        private String conversationId;
        private ModelType modelType;
        private String modelName;
        private List<ChatMessage> history;
        private Map<String, Object> metadata;
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
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
        
        public Builder history(List<ChatMessage> history) {
            this.history = history;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public ChatRequest build() {
            return new ChatRequest(message, conversationId, modelType, modelName, history, metadata);
        }
    }
}
