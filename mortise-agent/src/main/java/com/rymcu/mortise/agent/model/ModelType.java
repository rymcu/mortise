package com.rymcu.mortise.agent.model;

/**
 * 支持的 AI 模型类型枚举
 */
public enum ModelType {
    
    OPENAI("openai", "OpenAI GPT"),
    ANTHROPIC("anthropic", "Anthropic Claude"),
    DEEPSEEK("deepseek", "DeepSeek"),
    ZHIPU("zhipu", "智谱 GLM"),
    DASHSCOPE("dashscope", "阿里云通义千问"),
    OLLAMA("ollama", "Ollama 本地模型");
    
    private final String code;
    private final String displayName;
    
    ModelType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown model type: " + code);
    }
}
