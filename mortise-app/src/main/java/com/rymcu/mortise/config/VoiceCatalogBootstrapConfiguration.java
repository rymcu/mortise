package com.rymcu.mortise.config;

import com.rymcu.mortise.voice.application.service.bootstrap.VoiceCatalogBootstrapService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 在应用完全就绪后补齐语音目录，避免部署环境遗漏启动补数。
 */
@Configuration(proxyBeanMethods = false)
public class VoiceCatalogBootstrapConfiguration {

    @Bean
    public ApplicationListener<ApplicationReadyEvent> voiceCatalogBootstrapReadyListener(
            VoiceCatalogBootstrapService voiceCatalogBootstrapService
    ) {
        return event -> voiceCatalogBootstrapService.bootstrapIfNecessary();
    }
}
