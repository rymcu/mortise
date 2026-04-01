package com.rymcu.mortise.monitor.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库性能监控配置
 * 提供数据库连接池状态监控和性能指标收集
 * 
 * @author ronger
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnClass(HikariDataSource.class)
public class DatabasePerformanceConfig {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;

    /**
     * 使用@Lazy注解避免循环依赖
     */
    public DatabasePerformanceConfig(@Lazy DataSource dataSource, @Lazy MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 注册数据库连接池 Gauge（在 Bean 初始化后调用，避免重复注册）
     * 由于 DataSource 可能尚未完全初始化，所以在首次使用时延迟注册
     */
    private volatile boolean gaugesRegistered = false;
    
    private void registerConnectionPoolGauges(HikariDataSource hikariDataSource) {
        if (gaugesRegistered) {
            return;
        }
        
        synchronized (this) {
            if (gaugesRegistered) {
                return;
            }
            
            try {
                // 注册活跃连接数 Gauge
                meterRegistry.gauge("hikari.connections.active", hikariDataSource, ds -> {
                    HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
                    return poolMXBean != null ? poolMXBean.getActiveConnections() : 0;
                });

                // 注册空闲连接数 Gauge
                meterRegistry.gauge("hikari.connections.idle", hikariDataSource, ds -> {
                    HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
                    return poolMXBean != null ? poolMXBean.getIdleConnections() : 0;
                });

                // 注册总连接数 Gauge
                meterRegistry.gauge("hikari.connections.total", hikariDataSource, ds -> {
                    HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
                    return poolMXBean != null ? poolMXBean.getTotalConnections() : 0;
                });

                // 注册最大连接数 Gauge
                meterRegistry.gauge("hikari.connections.max", hikariDataSource, HikariDataSource::getMaximumPoolSize);

                // 注册最小空闲连接数 Gauge
                meterRegistry.gauge("hikari.connections.min", hikariDataSource, HikariDataSource::getMinimumIdle);

                // 注册等待线程数 Gauge
                meterRegistry.gauge("hikari.threads.awaiting", hikariDataSource, ds -> {
                    HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
                    return poolMXBean != null ? poolMXBean.getThreadsAwaitingConnection() : 0;
                });

                // 注册连接池使用率 Gauge
                meterRegistry.gauge("hikari.connections.usage", hikariDataSource, ds -> {
                    HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
                    if (poolMXBean == null) {
                        return 0.0;
                    }
                    int maxSize = ds.getMaximumPoolSize();
                    return maxSize > 0 ? (double) poolMXBean.getActiveConnections() / maxSize : 0.0;
                });

                gaugesRegistered = true;
                log.debug("数据库连接池 Gauge 指标注册成功");
            } catch (Exception e) {
                log.error("注册数据库连接池 Gauge 失败", e);
            }
        }
    }

    /**
     * HikariCP 连接池健康检查指示器
     */
    @Bean
    public HealthIndicator hikariHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    if (dataSource instanceof HikariDataSource hikariDataSource) {
                        // 检查连接池是否已启动
                        if (!hikariDataSource.isRunning()) {
                            return Health.down()
                                    .withDetail("database", "连接池未启动")
                                    .build();
                        }

                        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                        if (poolMXBean == null) {
                            return Health.up()
                                    .withDetail("database", "PostgreSQL")
                                    .withDetail("status", "连接正常")
                                    .build();
                        }

                        return Health.up()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("poolName", hikariDataSource.getPoolName())
                                .withDetail("activeConnections", poolMXBean.getActiveConnections())
                                .withDetail("totalConnections", poolMXBean.getTotalConnections())
                                .withDetail("idleConnections", poolMXBean.getIdleConnections())
                                .withDetail("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection())
                                .withDetail("maxPoolSize", hikariDataSource.getMaximumPoolSize())
                                .withDetail("minIdle", hikariDataSource.getMinimumIdle())
                                .build();
                    }
                    return Health.up().withDetail("database", "Available").build();
                }
            } catch (SQLException e) {
                log.error("数据库健康检查失败", e);
                return Health.down()
                        .withDetail("database", "Unavailable")
                        .withDetail("error", e.getMessage())
                        .build();
            }
            return Health.down().withDetail("database", "Connection invalid").build();
        };
    }

    /**
     * 数据库连接池监控和告警
     * 注意：Gauge 指标在首次调用时延迟注册，此方法负责告警检查
     */
    @Scheduled(fixedRate = 30000) // 30秒检查一次
    public void monitorConnectionPoolAlerts() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            try {
                // 检查连接池是否已经启动
                if (!hikariDataSource.isRunning()) {
                    log.debug("HikariCP连接池尚未启动，跳过监控");
                    return;
                }

                // 首次调用时注册 Gauge
                if (!gaugesRegistered) {
                    registerConnectionPoolGauges(hikariDataSource);
                }

                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                if (poolMXBean == null) {
                    log.debug("HikariPoolMXBean尚未可用，跳过监控");
                    return;
                }

                // 连接池使用率
                int maxSize = hikariDataSource.getMaximumPoolSize();
                double usage = maxSize > 0 ? (double) poolMXBean.getActiveConnections() / maxSize : 0.0;

                // 如果连接池使用率过高，记录警告
                if (usage > 0.8) {
                    log.warn("⚠️ 数据库连接池使用率较高: {}%, 活跃连接: {}/{}",
                            Math.round(usage * 100),
                            poolMXBean.getActiveConnections(),
                            maxSize);
                }

                // 如果有等待线程，记录警告
                if (poolMXBean.getThreadsAwaitingConnection() > 0) {
                    log.warn("⚠️ 有 {} 个线程正在等待数据库连接", 
                            poolMXBean.getThreadsAwaitingConnection());
                }

            } catch (Exception e) {
                log.error("数据库连接池监控失败", e);
            }
        }
    }

    /**
     * 数据库连接池状态日志定时输出
     */
    @Scheduled(fixedRate = 300000) // 5分钟输出一次状态日志
    public void logConnectionPoolStatus() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            try {
                // 检查连接池是否已经启动
                if (!hikariDataSource.isRunning()) {
                    log.debug("HikariCP连接池尚未启动，跳过状态日志");
                    return;
                }

                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                if (poolMXBean == null) {
                    log.debug("HikariPoolMXBean尚未可用，跳过状态日志");
                    return;
                }

                int activeConnections = poolMXBean.getActiveConnections();
                int idleConnections = poolMXBean.getIdleConnections();
                int totalConnections = poolMXBean.getTotalConnections();
                int threadsAwaiting = poolMXBean.getThreadsAwaitingConnection();
                int maxPoolSize = hikariDataSource.getMaximumPoolSize();
                int minIdle = hikariDataSource.getMinimumIdle();

                double usagePercent = (double) activeConnections / maxPoolSize * 100;

                log.info("💾 数据库连接池状态 [{}] - " +
                                "活跃: {}, 空闲: {}, 总计: {}/{}, " +
                                "等待: {}, 最小空闲: {}, 使用率: {:.1f}%",
                        hikariDataSource.getPoolName(),
                        activeConnections,
                        idleConnections,
                        totalConnections,
                        maxPoolSize,
                        threadsAwaiting,
                        minIdle,
                        usagePercent);

            } catch (Exception e) {
                log.error("输出数据库连接池状态失败", e);
            }
        }
    }
}
