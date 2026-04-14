package com.rymcu.mortise.voice.application.bootstrap;

import com.rymcu.mortise.voice.application.service.bootstrap.VoiceCatalogBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时同步系统贡献的默认语音目录。
 */
@Component
@RequiredArgsConstructor
public class VoiceCatalogBootstrapRunner implements ApplicationRunner {

    private final VoiceCatalogBootstrapService voiceCatalogBootstrapService;

    @Override
    public void run(ApplicationArguments args) {
        voiceCatalogBootstrapService.bootstrapIfNecessary();
    }
}
