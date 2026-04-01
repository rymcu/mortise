package com.rymcu.mortise.agent.kernel.config;

import com.rymcu.mortise.agent.kernel.constant.AgentConstants;
import com.rymcu.mortise.agent.kernel.model.ModelType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent 模块配置属性。
 */
@ConfigurationProperties(prefix = "mortise.agent")
public record AgentProperties(
        ModelConfig defaultModel,
        RouterConfig router,
        ReActConfig react
) {

    public AgentProperties {
        defaultModel = defaultModel != null ? defaultModel : new ModelConfig(ModelType.OPENAI, AgentConstants.AGENT_DEFAULT_MODEL, null);
        router = router != null ? router : new RouterConfig(null, IntentClassifierType.FUNCTION_CALLING, null, AgentConstants.DEFAULT_CONFIDENCE_THRESHOLD);
        react = react != null ? react : new ReActConfig(AgentConstants.DEFAULT_MAX_ITERATIONS, true, true);
    }

    public enum IntentClassifierType {
        RULE_BASED,
        FUNCTION_CALLING,
        LLM_BASED
    }

    public record ModelConfig(
            ModelType type,
            String name,
            String apiKey
    ) {
    }

    public record RouterConfig(
            ModelConfig intentClassifierModel,
            IntentClassifierType intentClassifierType,
            String intentClassifierPrompt,
            double confidenceThreshold
    ) {
        public RouterConfig {
            confidenceThreshold = confidenceThreshold > 0 ? confidenceThreshold : AgentConstants.DEFAULT_CONFIDENCE_THRESHOLD;
            intentClassifierType = intentClassifierType != null ? intentClassifierType : IntentClassifierType.FUNCTION_CALLING;
        }
    }

    public record ReActConfig(
            int maxIterations,
            boolean enableSelfReflection,
            boolean enableErrorRecovery
    ) {
        public ReActConfig {
            maxIterations = maxIterations > 0 ? maxIterations : AgentConstants.DEFAULT_MAX_ITERATIONS;
        }
    }
}
