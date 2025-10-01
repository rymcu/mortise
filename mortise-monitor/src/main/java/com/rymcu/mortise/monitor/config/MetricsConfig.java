package com.rymcu.mortise.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 监控配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
public class MetricsConfig {

    /**
     * 自定义 Meter Registry
     * 添加公共标签
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags(
                    "application", "mortise",
                    "env", System.getProperty("spring.profiles.active", "default")
            );
            log.info("Micrometer 监控配置已加载");
        };
    }
}
