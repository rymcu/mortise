package com.rymcu.mortise.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 优化后的异步任务线程池配置
 * 为所有模块提供统一的异步任务执行器
 * 
 * @author ronger
 * @email ronger-x@outlook.com
 */
@Slf4j
@Configuration
public class TaskExecutorConfig {

    @Value("${executor.thread.async.corePoolSize:10}")
    private int corePoolSize;

    @Value("${executor.thread.async.maxPoolSize:20}")
    private int maxPoolSize;

    @Value("${executor.thread.async.queueCapacity:200}")
    private int queueCapacity;

    @Value("${executor.thread.async.keepAliveSeconds:60}")
    private int keepAliveSeconds;

    @Value("${executor.thread.async.threadNamePrefix:mortise-async-}")
    private String threadNamePrefix;

    /**
     * 自定义异步任务线程池
     * 
     * @return TaskExecutor 异步任务执行器
     */
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 任务队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程名称前缀
        executor.setThreadNamePrefix(threadNamePrefix);

        // 拒绝策略配置
        // AbortPolicy: 直接抛出异常，适用于对任务丢失敏感的场景
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        // CallerRunsPolicy: 更安全的生产环境策略
        // 当线程池和队列都满了之后，这个策略不会丢弃任务，也不会抛出异常
        // 而是由提交该任务的线程（通常是处理 Web 请求的 Tomcat 线程）自己来同步执行这个任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(keepAliveSeconds);

        executor.initialize();

        log.info("自定义异步任务线程池'{}'初始化完成, 核心线程数:{}, 最大线程数:{}, 队列容量:{}",
                threadNamePrefix, corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}