package com.rymcu.mortise.system.health;

import com.rymcu.mortise.system.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统业务健康检查指标
 * 监控用户活跃度、登录成功率、系统负载等业务指标
 * 
 * @author ronger
 */
@Slf4j
@Component
public class SystemBusinessHealthIndicator implements HealthIndicator {

    private final UserService userService;
    private final MeterRegistry meterRegistry;
    
    // 业务统计计数器
    private final AtomicLong loginAttempts = new AtomicLong(0);
    private final AtomicLong loginSuccesses = new AtomicLong(0);
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong newRegistrations = new AtomicLong(0);
    
    // 最后更新时间
    private volatile LocalDateTime lastStatsUpdate = LocalDateTime.now();

    public SystemBusinessHealthIndicator(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            // 获取统计数据
            long attempts = loginAttempts.get();
            long successes = loginSuccesses.get();
            long active = activeUsers.get();
            long registrations = newRegistrations.get();

            // 计算登录成功率
            double loginSuccessRate = attempts > 0 ? (double) successes / attempts * 100 : 100;

            // 获取实时数据库统计（这里需要根据实际 UserService 方法调整）
            long totalUsers = getUserCount();
            long onlineUsers = getOnlineUserCount();

            // 记录指标到 Micrometer
            meterRegistry.gauge("system.login.success.rate", loginSuccessRate);
            meterRegistry.gauge("system.users.total", totalUsers);
            meterRegistry.gauge("system.users.online", onlineUsers);
            meterRegistry.gauge("system.users.active", active);
            meterRegistry.gauge("system.new.registrations", registrations);

            // 健康状态判断
            boolean isHealthy = loginSuccessRate >= 80.0 && 
                               onlineUsers <= totalUsers * 0.3; // 在线用户不超过总用户30%（避免异常高并发）

            Health.Builder builder = isHealthy ? Health.up() : Health.down();

            return builder
                    .withDetail("loginSuccessRate", String.format("%.2f%%", loginSuccessRate))
                    .withDetail("loginAttempts", attempts)
                    .withDetail("loginSuccesses", successes)
                    .withDetail("totalUsers", totalUsers)
                    .withDetail("onlineUsers", onlineUsers)
                    .withDetail("activeUsers", active)
                    .withDetail("newRegistrations", registrations)
                    .withDetail("onlineUserRatio", String.format("%.2f%%", 
                            totalUsers > 0 ? (double) onlineUsers / totalUsers * 100 : 0))
                    .withDetail("lastStatsUpdate", lastStatsUpdate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .withDetail("systemLoad", isHealthy ? "正常" : "需要关注")
                    .build();

        } catch (Exception e) {
            log.error("系统业务健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 记录登录尝试
     */
    public void recordLoginAttempt() {
        loginAttempts.incrementAndGet();
        lastStatsUpdate = LocalDateTime.now();
    }

    /**
     * 记录登录成功
     */
    public void recordLoginSuccess() {
        loginSuccesses.incrementAndGet();
        lastStatsUpdate = LocalDateTime.now();
    }

    /**
     * 记录活跃用户
     */
    public void recordActiveUser() {
        activeUsers.incrementAndGet();
        lastStatsUpdate = LocalDateTime.now();
    }

    /**
     * 记录新用户注册
     */
    public void recordNewRegistration() {
        newRegistrations.incrementAndGet();
        lastStatsUpdate = LocalDateTime.now();
    }

    /**
     * 定时重置统计数据
     */
    @Scheduled(fixedRate = 86400000) // 24小时重置一次
    public void resetStatistics() {
        long attempts = loginAttempts.getAndSet(0);
        long successes = loginSuccesses.getAndSet(0);
        long active = activeUsers.getAndSet(0);
        long registrations = newRegistrations.getAndSet(0);
        
        log.info("系统业务统计数据已重置 - 登录尝试: {}, 登录成功: {}, 活跃用户: {}, 新注册: {}", 
                attempts, successes, active, registrations);
    }

    /**
     * 获取用户总数（需要根据实际 UserService 实现）
     */
    private long getUserCount() {
        try {
            // 这里需要根据实际的 UserService 方法实现
            // return userService.getTotalUserCount();
            return 1000; // 示例值
        } catch (Exception e) {
            log.warn("获取用户总数失败", e);
            return 0;
        }
    }

    /**
     * 获取在线用户数（需要根据实际 UserService 实现）
     */
    private long getOnlineUserCount() {
        try {
            // 这里需要根据实际的 UserService 方法实现
            // return userService.getOnlineUserCount();
            return 50; // 示例值
        } catch (Exception e) {
            log.warn("获取在线用户数失败", e);
            return 0;
        }
    }
}