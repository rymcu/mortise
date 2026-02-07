package com.rymcu.mortise.auth.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SecurityContext 传播装饰器
 * <p>
 * 将提交任务时的 SecurityContext 传播到异步执行线程中，
 * 解决 @Async 方法和线程池任务中无法获取当前用户信息的问题。
 * </p>
 *
 * <p><b>工作原理：</b></p>
 * <ol>
 *   <li>在主线程提交任务时，捕获当前 SecurityContext</li>
 *   <li>在异步线程执行任务前，设置捕获的 SecurityContext</li>
 *   <li>任务执行完毕后，恢复原有 SecurityContext（避免线程复用导致的上下文泄漏）</li>
 * </ol>
 *
 * <p><b>模块解耦设计：</b></p>
 * <ul>
 *   <li>本类位于 auth 模块，依赖 Spring Security</li>
 *   <li>通过 Spring Bean（{@link Component}）注册到容器</li>
 *   <li>core 模块的 TaskExecutorConfig 通过 {@code @Autowired(required = false)}
 *       可选注入 {@link TaskDecorator}，无需直接依赖 auth 模块</li>
 * </ul>
 *
 * <p><b>适用场景：</b></p>
 * <ul>
 *   <li>@Async 注解的异步方法</li>
 *   <li>ThreadPoolTaskExecutor 提交的任务</li>
 *   <li>所有需要在异步线程中访问 CurrentUserUtils 的场景</li>
 * </ul>
 *
 * @author ronger
 * @since 0.2.0
 */
@Component
public class SecurityContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 在主线程中捕获 SecurityContext
        SecurityContext context = SecurityContextHolder.getContext();

        return () -> {
            // 在异步线程中恢复 SecurityContext
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                runnable.run();
            } finally {
                // 恢复原有上下文，避免线程复用时的上下文泄漏
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }
}
