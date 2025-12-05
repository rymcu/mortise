package com.rymcu.mortise;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Mortise 主应用程序
 * <p>
 * 多模块架构说明：
 * - mortise-common: 公共工具和基础类
 * - mortise-core: 核心响应模型
 * - mortise-log: 日志模块 (SPI: LogStorage)
 * - mortise-cache: 缓存模块 (SPI: CacheConfigurer)
 * - mortise-notification: 通知模块 (SPI: NotificationSender)
 * - mortise-auth: 认证授权模块 (SPI: SecurityConfigurer)
 * - mortise-web: Web 配置模块
 * - mortise-monitor: 监控模块
 * - mortise-system: 系统业务模块
 * - mortise-file: 文件管理模块
 * - mortise-persistence: 数据库扩展模块
 * - mortise-wechat: 微信集成模块 (扫码登录、消息推送)
 * - mortise-app: 主应用模块（当前）
 *
 * @author ronger
 */
@EnableAsync
@EnableFileStorage
@SpringBootApplication
public class MortiseApplication {

    public static void main(String[] args) {
        // 记录应用启动时间（用于监控启动耗时）
        System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));

        SpringApplication.run(MortiseApplication.class, args);
    }
}
