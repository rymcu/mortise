package com.rymcu.mortise.agent.application.prompt;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示词构建器
 * 使用 Spring AI 的 PromptTemplate 管理提示词
 */
public class PromptBuilder {
    
    private final Map<String, Object> params = new HashMap<>();
    private final AgentPromptTemplate templateProvider;
    
    public PromptBuilder(AgentPromptTemplate templateProvider) {
        this.templateProvider = templateProvider;
        // 设置默认值
        params.put("agent_name", AgentPromptConstants.DEFAULT_AGENT_NAME);
        params.put("team_name", AgentPromptConstants.DEFAULT_TEAM_NAME);
        params.put("user_name", AgentPromptConstants.DEFAULT_USER_NAME);
        params.put("role", AgentPromptConstants.DEFAULT_ROLE);
        params.put("role_introduction", AgentPromptConstants.DEFAULT_ROLE_INTRODUCTION);
        params.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * 设置 Agent 名称
     */
    public PromptBuilder agentName(String name) {
        params.put("agent_name", name);
        return this;
    }
    
    /**
     * 设置团队名称
     */
    public PromptBuilder teamName(String name) {
        params.put("team_name", name);
        return this;
    }
    
    /**
     * 设置用户名称
     */
    public PromptBuilder userName(String name) {
        params.put("user_name", name);
        return this;
    }
    
    /**
     * 设置角色
     */
    public PromptBuilder role(String role) {
        params.put("role", role);
        return this;
    }
    
    /**
     * 设置角色介绍
     */
    public PromptBuilder roleIntroduction(String introduction) {
        params.put("role_introduction", introduction);
        return this;
    }
    
    /**
     * 设置当前时间
     */
    public PromptBuilder time(String time) {
        params.put("time", time);
        return this;
    }
    
    /**
     * 设置工具描述
     */
    public PromptBuilder toolDescriptions(String descriptions) {
        params.put("tool_descriptions", descriptions);
        return this;
    }
    
    /**
     * 设置用户问题
     */
    public PromptBuilder question(String question) {
        params.put("question", question);
        return this;
    }
    
    /**
     * 设置当前步骤
     */
    public PromptBuilder currentStep(int step) {
        params.put("current_step", step);
        return this;
    }
    
    /**
     * 设置最大步骤
     */
    public PromptBuilder maxSteps(int max) {
        params.put("max_steps", max);
        return this;
    }
    
    /**
     * 设置工具列表（用于意图分类）
     */
    public PromptBuilder toolList(String toolList) {
        params.put("tool_list", toolList);
        return this;
    }
    
    /**
     * 设置用户消息（用于意图分类）
     */
    public PromptBuilder userMessage(String message) {
        params.put("user_message", message);
        return this;
    }
    
    /**
     * 设置记忆
     */
    public PromptBuilder memory(String memory) {
        params.put("memory_map", memory != null ? memory : "None");
        return this;
    }
    
    /**
     * 构建 ReAct 系统提示词字符串
     */
    public String buildReActPrompt() {
        return templateProvider.buildReActPrompt(params);
    }
    
    /**
     * 构建简单聊天系统提示词
     */
    public String buildChatPrompt() {
        return templateProvider.buildChatPrompt(params);
    }
    
    /**
     * 构建意图分类提示词
     */
    public String buildIntentClassificationPrompt() {
        return templateProvider.buildIntentClassificationPrompt(params);
    }
    
    /**
     * 构建意图分类 Prompt 对象（Spring AI 格式）
     */
    public Prompt buildIntentClassificationPromptAsPrompt() {
        return templateProvider.getIntentClassificationTemplate().create(params);
    }
    
    /**
     * 获取参数 Map
     */
    public Map<String, Object> getParams() {
        return new HashMap<>(params);
    }
    
    /**
     * 创建新的构建器实例
     */
    public static PromptBuilder create(AgentPromptTemplate templateProvider) {
        return new PromptBuilder(templateProvider);
    }
}
