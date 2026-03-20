package com.rymcu.mortise.agent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    // FunctionCallback beans 会被 Spring AI 自动发现和注册
    // 只需要定义返回 FunctionCallback 的 @Bean 方法
}