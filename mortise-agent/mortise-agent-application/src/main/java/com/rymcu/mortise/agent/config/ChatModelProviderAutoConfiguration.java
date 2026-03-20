package com.rymcu.mortise.agent.config;

import com.rymcu.mortise.agent.provider.ChatModelProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 模型提供者基础设施配置
 * 负责注册模型提供者和 Registry
 */
@Configuration
@EnableConfigurationProperties(AgentProperties.class)
public class ChatModelProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ChatModelProviderAutoConfiguration.class);

    @Bean
    public ChatModelProviderRegistry chatModelProviderRegistry() {
        return new ChatModelProviderRegistry();
    }
    
    /**
     * OpenAI 模型提供者配置
     * 仅在 OpenAiChatModel 类存在且 Bean 存在时生效
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.openai.OpenAiChatModel")
    static class OpenAiConfiguration {
        
        @Bean
        @ConditionalOnBean(name = "openAiChatModel")
        public com.rymcu.mortise.agent.provider.OpenAiChatModelProvider openAiChatModelProvider(
                org.springframework.ai.openai.OpenAiChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering OpenAI chat model provider");
            String defaultModel = properties.defaultModel().type() != null && 
                properties.defaultModel().type().name().equals("OPENAI") 
                ? properties.defaultModel().name() : null;
            var provider = new com.rymcu.mortise.agent.provider.OpenAiChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }
    
    /**
     * Anthropic 模型提供者配置
     * 仅在 AnthropicChatModel 类存在且 Bean 存在时生效
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.anthropic.AnthropicChatModel")
    static class AnthropicConfiguration {
        
        @Bean
        @ConditionalOnBean(name = "anthropicChatModel")
        public com.rymcu.mortise.agent.provider.AnthropicChatModelProvider anthropicChatModelProvider(
                org.springframework.ai.anthropic.AnthropicChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering Anthropic chat model provider");
            String defaultModel = properties.defaultModel().type() != null && 
                properties.defaultModel().type().name().equals("ANTHROPIC") 
                ? properties.defaultModel().name() : null;
            var provider = new com.rymcu.mortise.agent.provider.AnthropicChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }

    /**
     * Ollama 模型提供者配置
     * 仅在 OllamaChatModel 类存在且 Bean 存在时生效
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.ollama.OllamaChatModel")
    static class OllamaConfiguration {
        
        @Bean
        @ConditionalOnBean(name = "ollamaChatModel")
        public com.rymcu.mortise.agent.provider.OllamaChatModelProvider ollamaChatModelProvider(
                org.springframework.ai.ollama.OllamaChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering Ollama chat model provider");
            String defaultModel = properties.defaultModel().type() != null && 
                properties.defaultModel().type().name().equals("OLLAMA") 
                ? properties.defaultModel().name() : null;
            var provider = new com.rymcu.mortise.agent.provider.OllamaChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }
}