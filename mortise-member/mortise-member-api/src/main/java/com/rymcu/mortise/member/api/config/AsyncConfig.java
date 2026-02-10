package com.rymcu.mortise.member.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务和定时任务配置（JDK 21 优化版）
 * <p>
 * 使用 JDK 21 虚拟线程作为默认异步执行器。
 * 如果容器中存在 {@link TaskDecorator} Bean（由 auth 模块提供），
 * 则自动应用上下文传播，确保异步线程可获取用户信息。
 * </p>
 *
 * @author ronger
 * @since 0.2.0
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    @Nullable
    @Autowired(required = false)
    private TaskDecorator taskDecorator;

    @Override
    public Executor getAsyncExecutor() {
        Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // 如果存在 TaskDecorator（如 SecurityContextTaskDecorator），则包装执行器
        if (taskDecorator != null) {
            Executor decorated = new DecoratedExecutor(virtualThreadExecutor, taskDecorator);
            log.info("API 模块异步执行器初始化完成 (JDK 21 Virtual Threads, TaskDecorator: {})",
                    taskDecorator.getClass().getSimpleName());
            return decorated;
        }

        log.info("API 模块异步执行器初始化完成 (JDK 21 Virtual Threads)");
        return virtualThreadExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("异步任务执行异常 - 方法: {}, 参数: {}", method.getName(), params, ex);
    }

    /**
     * 应用 TaskDecorator 的执行器包装
     *
     * @param delegate      底层执行器
     * @param taskDecorator 任务装饰器
     */
    private record DecoratedExecutor(Executor delegate, TaskDecorator taskDecorator) implements Executor {
        @Override
        public void execute(Runnable command) {
            delegate.execute(taskDecorator.decorate(command));
        }
    }
}
