package com.rymcu.mortise.agent.agent;

import com.rymcu.mortise.agent.config.AgentProperties;
import com.rymcu.mortise.agent.prompt.AgentPromptTemplate;
import com.rymcu.mortise.agent.spi.ChatModelProvider;
import org.springframework.stereotype.Component;

/**
 * ReAct Agent 工厂
 * 负责创建和配置 ReAct Agent 实例
 */
@Component
public class ReActAgentFactory {
    
    private final AgentPromptTemplate promptTemplate;
    private final AgentProperties properties;
    
    public ReActAgentFactory(AgentPromptTemplate promptTemplate, AgentProperties properties) {
        this.promptTemplate = promptTemplate;
        this.properties = properties;
    }
    
    /**
     * 创建 ReAct Agent
     */
    public ReActAgent create(ChatModelProvider modelProvider) {
        AgentProperties.ReActConfig config = properties.react();
        return new ReActAgent(
            modelProvider,
            promptTemplate,
            config.maxIterations(),
            config.enableSelfReflection()
        );
    }
    
    /**
     * 使用默认配置创建 ReAct Agent
     */
    public ReActAgent createDefault(ChatModelProvider modelProvider) {
        return new ReActAgent(
            modelProvider,
            promptTemplate,
            5,
            true
        );
    }
}