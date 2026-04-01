package com.rymcu.mortise.agent.infra.config;

import com.rymcu.mortise.agent.infra.provider.AnthropicChatModelProvider;
import com.rymcu.mortise.agent.infra.provider.OllamaChatModelProvider;
import com.rymcu.mortise.agent.infra.provider.OpenAiChatModelProvider;
import com.rymcu.mortise.agent.kernel.config.AgentProperties;
import com.rymcu.mortise.agent.kernel.provider.ChatModelProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 模型提供者基础设施配置。
 */
@Configuration
public class ChatModelProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ChatModelProviderAutoConfiguration.class);

    @Bean
    public ChatModelProviderRegistry chatModelProviderRegistry() {
        return new ChatModelProviderRegistry();
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.openai.OpenAiChatModel")
    static class OpenAiConfiguration {

        @Bean
        @ConditionalOnBean(name = "openAiChatModel")
        public OpenAiChatModelProvider openAiChatModelProvider(
                org.springframework.ai.openai.OpenAiChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering OpenAI chat model provider");
            String defaultModel = properties.defaultModel().type() != null
                    && properties.defaultModel().type().name().equals("OPENAI")
                    ? properties.defaultModel().name()
                    : null;
            OpenAiChatModelProvider provider = new OpenAiChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.anthropic.AnthropicChatModel")
    static class AnthropicConfiguration {

        @Bean
        @ConditionalOnBean(name = "anthropicChatModel")
        public AnthropicChatModelProvider anthropicChatModelProvider(
                org.springframework.ai.anthropic.AnthropicChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering Anthropic chat model provider");
            String defaultModel = properties.defaultModel().type() != null
                    && properties.defaultModel().type().name().equals("ANTHROPIC")
                    ? properties.defaultModel().name()
                    : null;
            AnthropicChatModelProvider provider = new AnthropicChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.ai.ollama.OllamaChatModel")
    static class OllamaConfiguration {

        @Bean
        @ConditionalOnBean(name = "ollamaChatModel")
        public OllamaChatModelProvider ollamaChatModelProvider(
                org.springframework.ai.ollama.OllamaChatModel chatModel,
                ChatModelProviderRegistry registry,
                AgentProperties properties
        ) {
            log.info("Registering Ollama chat model provider");
            String defaultModel = properties.defaultModel().type() != null
                    && properties.defaultModel().type().name().equals("OLLAMA")
                    ? properties.defaultModel().name()
                    : null;
            OllamaChatModelProvider provider = new OllamaChatModelProvider(chatModel, defaultModel);
            registry.register(provider);
            return provider;
        }
    }
}
