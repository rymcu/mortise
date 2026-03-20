package com.rymcu.mortise.agent.constant;

/**
 * Agent 模块常量定义
 * 集中管理所有配置默认值，消除散落在代码中的魔法值
 */
public final class AgentConstants {

    private AgentConstants() {
    }

    // ==================== 置信度阈值 ====================

    /** 高置信度阈值 */
    public static final double HIGH_CONFIDENCE_THRESHOLD = 0.8;
    /** 默认置信度阈值（路由决策） */
    public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.7;

    // ==================== 分类器置信度 ====================

    /** 规则分类器 - 匹配工具关键词的置信度 */
    public static final double RULE_TOOL_KEYWORD_CONFIDENCE = 0.7;
    /** 规则分类器 - 匹配聊天关键词的置信度 */
    public static final double RULE_CHAT_KEYWORD_CONFIDENCE = 0.8;
    /** 规则分类器 - 匹配问句模式的置信度 */
    public static final double RULE_QUESTION_PATTERN_CONFIDENCE = 0.6;
    /** 规则分类器 - 无匹配的默认置信度 */
    public static final double RULE_DEFAULT_CONFIDENCE = 0.5;
    /** Function Calling 分类器 - 模型直接回复的置信度 */
    public static final double FC_DIRECT_REPLY_CONFIDENCE = 0.9;
    /** 分类器回退（异常）时的置信度 */
    public static final double FALLBACK_CONFIDENCE = 0.5;

    // ==================== ReAct Agent ====================

    /** 默认最大迭代次数 */
    public static final int DEFAULT_MAX_ITERATIONS = 5;

    // ==================== Function Calling ====================

    /** 意图指示函数名 */
    public static final String INTENT_FUNCTION_NAME = "indicate_tool_intent";

    // ==================== 提供商默认模型 ====================

    /** OpenAI 默认模型 */
    public static final String OPENAI_DEFAULT_MODEL = "gpt-4.1";
    /** Anthropic 默认模型 */
    public static final String ANTHROPIC_DEFAULT_MODEL = "claude-sonnet-4-20250514";
    /** Ollama 默认模型 */
    public static final String OLLAMA_DEFAULT_MODEL = "qwen2.5";
    /** Agent 全局默认模型 */
    public static final String AGENT_DEFAULT_MODEL = "gpt-4o-mini";
}
