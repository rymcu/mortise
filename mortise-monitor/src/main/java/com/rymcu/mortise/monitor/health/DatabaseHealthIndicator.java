package com.rymcu.mortise.monitor.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

/**
 * 数据库健康检查指示器
 *
 * @author ronger
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public DatabaseHealthIndicator(Optional<DataSource> dataSourceOptional) {
        this.dataSource = dataSourceOptional.orElse(null);
    }

    @Override
    public Health health() {
        if (dataSource == null) {
            return Health.unknown()
                    .withDetail("reason", "DataSource 未配置")
                    .build();
        }

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(3)) {
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
                
                return Health.up()
                        .withDetail("database", databaseProductName)
                        .withDetail("version", databaseProductVersion)
                        .withDetail("status", "连接正常")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "连接验证失败")
                        .build();
            }
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return Health.down()
                    .withDetail("database", "连接失败")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
