package com.rymcu.mortise.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 优化后的异步任务线程池配置
 *
 * @author ronger
 * @email ronger-x@outlook.com
 */
@Slf4j
@Configuration
public class TaskExecutorConfig {

    @Value("${executor.thread.async.corePoolSize}")
    private int corePoolSize;
    @Value("${executor.thread.async.maxPoolSize}")
    private int maxPoolSize;
    @Value("${executor.thread.async.queueCapacity}")
    private int queueCapacity;
    @Value("${executor.thread.async.keepAliveSeconds}")
    private int keepAliveSeconds;
    @Value("${executor.thread.async.name.prefix}")
    private String threadNamePrefix;

    /**
     * 定义一个全局共享的异步任务执行器。
     * 使用 @Bean("taskExecutor") 或默认方法名 "taskExecutor"
     * 当在方法上使用 @Async 时，Spring会默认寻找这个Bean。
     *
     * @return TaskExecutor
     */
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);

        // AbortPolicy
        // 这是默认策略。当池和队列都满时，将抛出 RejectedExecutionException。
        // 这种策略让调用者能感知到线程池已满，并可以根据业务场景进行处理（如：记录日志、稍后重试、返回错误信息给用户）。
        // 相较于CallerRunsPolicy，它避免了阻塞主线程，对 Web 服务更友好。
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // CallerRunsPolicy
        // 更安全的生产环境策略。当线程池和队列都满了之后，这个策略不会丢弃任务，也不会抛出异常。
        // 而是由提交该任务的线程（通常是处理 Web 请求的 Tomcat 线程）自己来同步执行这个任务。
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
