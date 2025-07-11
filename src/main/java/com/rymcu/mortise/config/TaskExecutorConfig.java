package com.rymcu.mortise.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 优化后的异步任务线程池配置
 *
 * @author ronger
 * @email ronger-x@outlook.com
 */
@Configuration
public class TaskExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorConfig.class);

    // 从application.yml中注入配置
    @Value("${executor.thread.async.corePoolSize}")
    private int corePoolSize;
    @Value("${executor.thread.async.maxPoolSize}")
    private int maxPoolSize;
    @Value("${executor.thread.async.queueCapacity}")
    private int queueCapacity;
    @Value("${executor.thread.async.name.prefix}")
    private String threadNamePrefix;

    /**
     * 定义一个全局共享的异步任务执行器。
     * 使用 @Bean("taskExecutor") 或默认方法名 "taskExecutor"
     * 当在方法上使用 @Async 时，Spring会默认寻找这个Bean。
     *
     * @return AsyncTaskExecutor
     */
    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);

        // 设置拒绝策略：AbortPolicy
        // 这是默认策略。当池和队列都满时，将抛出RejectedExecutionException。
        // 这种策略让调用者能感知到线程池已满，并可以根据业务场景进行处理（如：记录日志、稍后重试、返回错误信息给用户）。
        // 相较于CallerRunsPolicy，它避免了阻塞主线程，对Web服务更友好。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // 如果你确实需要 CallerRunsPolicy 的行为，可以保留它，但务必清楚其风险。
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        logger.info("自定义线程池 '{}' 初始化完成。", threadNamePrefix);
        return executor;
    }
}
