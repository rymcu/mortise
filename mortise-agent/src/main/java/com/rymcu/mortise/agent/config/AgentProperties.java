package com.rymcu.mortise.agent.config;

import com.rymcu.mortise.agent.model.ModelType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent 模块配置属性
 */
@ConfigurationProperties(prefix = "mortise.agent")
public record AgentProperties(
    ModelConfig defaultModel,
    RouterConfig router,
    ReActConfig react
) {
    
    public AgentProperties {
        defaultModel = defaultModel != null ? defaultModel : new ModelConfig(ModelType.OPENAI, "gpt-4o-mini", null);
        router = router != null ? router : new RouterConfig(null, IntentClassifierType.FUNCTION_CALLING, null, 0.7);
        react = react != null ? react : new ReActConfig(5, true, true);
    }
    
    /**
     * 意图分类器类型
     */
    public enum IntentClassifierType {
        /**
         * 规则匹配分类器（最快，无需LLM调用）
         */
        RULE_BASED,
        /**
         * 基于Function Calling的分类器（推荐）
         * 利用LLM原生的函数调用能力判断意图
         * 普通聊天直接返回，工具调用才走ReAct
         */
        FUNCTION_CALLING,
        /**
         * 基于LLM的分类器（需要额外LLM调用）
         */
        LLM_BASED
    }
    
    public record ModelConfig(
        ModelType type,
        String name,
        String apiKey
    ) {}
    
    public record RouterConfig(
        ModelConfig intentClassifierModel,
        IntentClassifierType intentClassifierType,
        String intentClassifierPrompt,
        double confidenceThreshold
    ) {
        public RouterConfig {
            confidenceThreshold = confidenceThreshold > 0 ? confidenceThreshold : 0.7;
            intentClassifierType = intentClassifierType != null ? intentClassifierType : IntentClassifierType.FUNCTION_CALLING;
        }
    }
    
    public record ReActConfig(
        int maxIterations,
        boolean enableSelfReflection,
        boolean enableErrorRecovery
    ) {
        public ReActConfig {
            maxIterations = maxIterations > 0 ? maxIterations : 5;
        }
    }
}
