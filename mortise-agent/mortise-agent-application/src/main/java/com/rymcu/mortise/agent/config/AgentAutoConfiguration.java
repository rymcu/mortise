package com.rymcu.mortise.agent.config;

import com.rymcu.mortise.agent.prompt.AgentPromptTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 自动配置类
 *
 * Spring AI 的 FunctionCallback 通过 Spring Bean 自动注册
 * 只需要定义 @Bean 返回 FunctionCallback 即可
 */
@Configuration
@EnableConfigurationProperties(AgentProperties.class)
public class AgentAutoConfiguration {

    @Bean
    public AgentPromptTemplate agentPromptTemplate() {
        return new AgentPromptTemplate();
    }
}