package com.rymcu.mortise.voice.infra.config;

import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 语音基础设施配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(VoiceProperties.class)
public class VoiceInfrastructureConfiguration {
}