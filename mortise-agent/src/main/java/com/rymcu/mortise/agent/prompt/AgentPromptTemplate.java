package com.rymcu.mortise.agent.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent 提示词模板管理
 * 使用 Spring AI 内置的 PromptTemplate 管理提示词
 */
@Component
public class AgentPromptTemplate {
    
    /**
     * ReAct 系统提示词模板
     * 适配 Spring AI 的原生 Function Calling 机制
     */
    private static final String REACT_SYSTEM_TEMPLATE = """
        ## Identity
        You are {agent_name}, developed by the {team_name} team.
        Your role: {role}. {role_introduction}
        The user's name: {user_name}.
        Current time: {time}.
        
        ## Instructions
        You are on step **{current_step}** of **{max_steps}**.
        
        When you need to use tools:
        1. Think about which tool is most appropriate
        2. Call the tool with proper arguments (the system will handle function calling)
        3. After receiving the tool result, decide if you need more information or can provide the final answer
        4. If this is the final step (current_step == max_steps), you MUST provide a final answer directly
        
        ## Guidelines
        - Call tools when you need to perform actions or retrieve real-time information
        - Provide direct answers when tools are not necessary
        - Keep responses concise and match the user's language
        - If a tool returns an error, try to recover or explain the limitation
        
        ## Response Rules
        - When you have enough information, provide a clear and helpful final answer
        - Do not mention the step count or internal process to the user
        - Stay in character as {role}
        """;
    
    /**
     * 简单聊天系统提示词模板
     */
    private static final String CHAT_SYSTEM_TEMPLATE = """
        You are {agent_name}, developed by the {team_name} team.
        Your role: {role}. {role_introduction}
        The user's name: {user_name}.
        Current time: {time}.
        
        Respond naturally and helpfully. Match the user's language.
        Keep responses concise and friendly.
        """;
    
    /**
     * 意图分类提示词模板
     */
    private static final String INTENT_CLASSIFICATION_TEMPLATE = """
        You are an intent classifier. Analyze the user input and determine the intent type.
        
        Available tools:
        {tool_list}
        
        Intent types:
        1. chat - Normal conversation: user wants to chat, ask questions, or get information without tool calls
        2. tool_call - Tool invocation: user explicitly needs to perform an action requiring tool execution
        
        Return result in JSON format only:
        {{
          "intent": "chat" or "tool_call",
          "confidence": <0.0 to 1.0>,
          "reasoning": "<brief explanation>",
          "recommendedTools": ["<tool names, only when intent is tool_call>"]
        }}
        
        User input: {user_message}
        """;
    
    /**
     * 工具描述模板
     */
    private static final String TOOL_DESCRIPTION_TEMPLATE = """
        - {tool_name}: {tool_description}
          Schema: {tool_schema}
        """;
    
    private final SystemPromptTemplate reactPromptTemplate;
    private final SystemPromptTemplate chatPromptTemplate;
    private final PromptTemplate intentClassificationTemplate;
    private final PromptTemplate toolDescriptionTemplate;
    
    public AgentPromptTemplate() {
        this.reactPromptTemplate = new SystemPromptTemplate(REACT_SYSTEM_TEMPLATE);
        this.chatPromptTemplate = new SystemPromptTemplate(CHAT_SYSTEM_TEMPLATE);
        this.intentClassificationTemplate = new PromptTemplate(INTENT_CLASSIFICATION_TEMPLATE);
        this.toolDescriptionTemplate = new PromptTemplate(TOOL_DESCRIPTION_TEMPLATE);
    }
    
    /**
     * 构建 ReAct 系统提示
     */
    public String buildReActPrompt(Map<String, Object> params) {
        return reactPromptTemplate.render(params);
    }
    
    /**
     * 构建简单聊天系统提示
     */
    public String buildChatPrompt(Map<String, Object> params) {
        return chatPromptTemplate.render(params);
    }
    
    /**
     * 构建意图分类提示
     */
    public String buildIntentClassificationPrompt(Map<String, Object> params) {
        return intentClassificationTemplate.render(params);
    }
    
    /**
     * 构建工具描述
     */
    public String buildToolDescription(String toolName, String description, String schema) {
        return toolDescriptionTemplate.render(Map.of(
            "tool_name", toolName,
            "tool_description", description,
            "tool_schema", schema
        ));
    }
    
    /**
     * 获取 ReAct 提示模板
     */
    public SystemPromptTemplate getReactPromptTemplate() {
        return reactPromptTemplate;
    }
    
    /**
     * 获取意图分类提示模板
     */
    public PromptTemplate getIntentClassificationTemplate() {
        return intentClassificationTemplate;
    }
}
