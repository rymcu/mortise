package com.rymcu.mortise;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.scheduling.annotation.EnableAsync;


@Slf4j
@EnableFileStorage
@EnableAsync
@EnableEncryptableProperties
@SpringBootApplication
@MapperScan("com.rymcu.mortise.mapper")
public class MortiseApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.setProperty("app.start.time", String.valueOf(startTime));

        SpringApplication app = new SpringApplication(MortiseApplication.class);

        // 添加早期事件监听器 - 在容器初始化之前注册
        app.addListeners(event -> {
            if (event instanceof ApplicationStartingEvent) {
                // 使用System.out确保输出，因为此时日志系统可能还未初始化
                System.out.println("🚀 应用开始启动: " + new java.util.Date(startTime));
                log.info("应用开始启动: {}", new java.util.Date(startTime));
            }
        });

        app.run(args);
    }

}
