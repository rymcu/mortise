package com.rymcu.mortise.file.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 生产环境文件存储护栏，防止多副本部署时误写本地磁盘。
 */
@Slf4j
@Configuration
public class FileStorageClusterGuardConfiguration {

    @Bean
    public SmartInitializingSingleton fileStorageClusterGuard(Environment environment) {
        return () -> {
            if (!environment.acceptsProfiles(Profiles.of("prod"))) {
                return;
            }

            String defaultPlatform = environment.getProperty("dromara.x-file-storage.default-platform");
            if (!StringUtils.hasText(defaultPlatform)) {
                throw new IllegalStateException("prod 环境必须显式配置 dromara.x-file-storage.default-platform，且必须指向对象存储平台");
            }

            String normalizedPlatform = defaultPlatform.trim().toLowerCase(Locale.ROOT);
            if (normalizedPlatform.startsWith("local")) {
                throw new IllegalStateException("prod 环境禁止使用本地文件存储作为默认平台: " + defaultPlatform);
            }

            Binder binder = Binder.get(environment);
            List<?> localPlatforms = binder
                    .bind("dromara.x-file-storage.local-plus", Bindable.listOf(Map.class))
                    .orElse(List.of());

            boolean hasEnabledLocalPlatform = localPlatforms.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(platform -> platform.get("enable-storage"))
                    .map(String::valueOf)
                    .anyMatch(Boolean.TRUE.toString()::equalsIgnoreCase);
            if (hasEnabledLocalPlatform) {
                throw new IllegalStateException("prod 环境禁止启用 local-plus 存储，请改为对象存储平台");
            }

            log.info("文件存储集群护栏校验通过，当前默认平台: {}", defaultPlatform);
        };
    }
}
