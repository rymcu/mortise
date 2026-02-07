package com.rymcu.mortise.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器配置（JDK 21 优化版）
 * <p>
 * 提供两种执行器供不同场景使用：
 * <ul>
 *   <li><b>taskExecutor（默认/主执行器）</b>：基于 JDK 21 虚拟线程，适合 I/O 密集型任务。
 *       虚拟线程由 JVM 管理，创建和切换开销极低，可以创建数百万级实例。
 *       适用于：日志写入、邮件发送、通知推送、事件监听、HTTP 调用等 I/O 等待场景。</li>
 *   <li><b>platformThreadExecutor</b>：传统平台线程池，适合 CPU 密集型任务。
 *       有界队列 + 线程数上限，避免创建过多线程导致 CPU 争抢。
 *       适用于：数据计算、图片处理、加密解密等 CPU 运算场景。</li>
 * </ul>
 * </p>
 *
 * <p><b>默认行为：</b>所有 @Async 方法默认使用虚拟线程执行器。
 * 如需使用平台线程池，请指定 @Async("platformThreadExecutor")。</p>
 *
 * <p><b>上下文传播：</b>如果容器中存在 {@link TaskDecorator} Bean（如 auth 模块提供的
 * SecurityContextTaskDecorator），则自动应用到所有执行器，确保异步线程可以正确获取上下文信息。</p>
 *
 * @author ronger
 * @since 0.2.0
 */
@Slf4j
@Configuration
public class TaskExecutorConfig {

    @Value("${executor.thread.platform.corePoolSize:4}")
    private int corePoolSize;

    @Value("${executor.thread.platform.maxPoolSize:8}")
    private int maxPoolSize;

    @Value("${executor.thread.platform.queueCapacity:100}")
    private int queueCapacity;

    @Value("${executor.thread.platform.keepAliveSeconds:60}")
    private int keepAliveSeconds;

    @Value("${executor.thread.platform.threadNamePrefix:mortise-platform-}")
    private String platformThreadNamePrefix;

    /**
     * 可选的任务装饰器（由 auth 模块等提供，用于传播 SecurityContext 等上下文）
     */
    @Nullable
    @Autowired(required = false)
    private TaskDecorator taskDecorator;

    /**
     * 默认异步任务执行器 — 基于 JDK 21 虚拟线程
     * <p>
     * 所有 @Async 方法默认使用此执行器。虚拟线程特性：
     * <ul>
     *   <li>每个任务一个虚拟线程，无需配置线程数上限</li>
     *   <li>虚拟线程创建和销毁成本极低（约 1KB 栈内存 vs 平台线程 ~1MB）</li>
     *   <li>I/O 阻塞时自动让出载体线程，不浪费 CPU 资源</li>
     *   <li>无队列积压风险，不存在拒绝策略问题</li>
     * </ul>
     * </p>
     *
     * @return TaskExecutor 基于虚拟线程的异步任务执行器
     */
    @Primary
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        TaskExecutorAdapter executor = new TaskExecutorAdapter(
                Executors.newVirtualThreadPerTaskExecutor()
        );

        // 如果存在 TaskDecorator（如 SecurityContext 传播装饰器），则应用
        if (taskDecorator != null) {
            executor.setTaskDecorator(taskDecorator);
            log.info("虚拟线程异步任务执行器已配置 TaskDecorator: {}", taskDecorator.getClass().getSimpleName());
        }

        log.info("虚拟线程异步任务执行器初始化完成 (JDK 21 Virtual Threads)");

        return executor;
    }

    /**
     * 平台线程池执行器 — 用于 CPU 密集型任务
     * <p>
     * 使用场景：@Async("platformThreadExecutor")
     * <ul>
     *   <li>数据计算、批处理</li>
     *   <li>图片/视频处理</li>
     *   <li>加密解密操作</li>
     * </ul>
     * </p>
     *
     * @return TaskExecutor 基于平台线程的任务执行器
     */
    @Bean(name = "platformThreadExecutor")
    public TaskExecutor platformThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(platformThreadNamePrefix);

        // 如果存在 TaskDecorator，则应用
        if (taskDecorator != null) {
            executor.setTaskDecorator(taskDecorator);
        }

        // CallerRunsPolicy: 队列满时由调用线程执行，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 优雅关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(keepAliveSeconds);

        executor.initialize();

        log.info("平台线程池执行器'{}'初始化完成, 核心线程数:{}, 最大线程数:{}, 队列容量:{}",
                platformThreadNamePrefix, corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}