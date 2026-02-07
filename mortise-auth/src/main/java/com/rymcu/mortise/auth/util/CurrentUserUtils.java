package com.rymcu.mortise.auth.util;

import com.rymcu.mortise.core.model.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 当前用户工具类
 * <p>
 * 提供获取当前登录用户信息的便捷方法，封装了 Spring Security Context 的访问逻辑。
 * 通过 CurrentUser 接口实现模块解耦，避免业务模块直接依赖 system 模块。
 * </p>
 *
 * <p><b>JDK 21 特性优化：</b></p>
 * <ul>
 *   <li>Pattern Matching for instanceof - 简化类型检查和转换</li>
 *   <li>Record - 不可变的用户上下文快照，线程安全</li>
 *   <li>Virtual Threads - 支持虚拟线程的上下文传播</li>
 *   <li>Structured Concurrency 风格的 API 设计</li>
 * </ul>
 *
 * <p><b>多线程/并发安全使用方式：</b></p>
 * <pre>
 * // 方式1: 使用 UserSnapshot 在异步任务前捕获用户信息（推荐）
 * var snapshot = CurrentUserUtils.captureSnapshot();
 * CompletableFuture.runAsync(() -> {
 *     // 直接使用 snapshot，无需依赖 SecurityContext
 *     Long userId = snapshot.userId();
 * });
 *
 * // 方式2: 使用包装方法传播 SecurityContext
 * executor.submit(CurrentUserUtils.wrapRunnable(() -> {
 *     Long userId = CurrentUserUtils.getUserId(); // 正常工作
 * }));
 *
 * // 方式3: 使用虚拟线程执行任务
 * CurrentUserUtils.runInVirtualThread(() -> {
 *     Long userId = CurrentUserUtils.getUserId(); // 自动传播上下文
 * });
 *
 * // 方式4: 使用支持上下文传播的 CompletableFuture
 * CurrentUserUtils.supplyAsyncWithContext(() -> {
 *     return CurrentUserUtils.getUserId();
 * }).thenAccept(userId -> System.out.println(userId));
 * </pre>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li>依赖倒置原则：依赖 CurrentUser 接口而非具体实现</li>
 *   <li>单一职责原则：只负责获取当前用户信息</li>
 *   <li>开闭原则：对扩展开放，对修改关闭</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取当前用户ID
 * Long userId = CurrentUserUtils.getUserId();
 *
 * // 使用 Optional API（推荐）
 * CurrentUserUtils.findCurrentUser()
 *     .map(CurrentUser::getUserId)
 *     .ifPresent(id -> log.info("User ID: {}", id));
 *
 * // 获取当前用户对象
 * CurrentUser currentUser = CurrentUserUtils.getCurrentUser();
 * if (currentUser != null) {
 *     String username = currentUser.getUsername();
 *     String nickname = currentUser.getNickname();
 * }
 *
 * // 获取用户名
 * String username = CurrentUserUtils.getUsername();
 *
 * // 检查是否已登录
 * boolean authenticated = CurrentUserUtils.isAuthenticated();
 * </pre>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>在非 Web 请求上下文中调用可能返回 null</li>
 *   <li>异步线程中请使用 {@link #captureSnapshot()} 或包装方法</li>
 *   <li>虚拟线程中使用 {@link #runInVirtualThread(Runnable)} 自动传播上下文</li>
 *   <li>建议在业务代码中使用 {@link #findCurrentUser()} 进行空安全处理</li>
 * </ul>
 *
 * @author ronger
 * @since 0.1.1
 */
public final class CurrentUserUtils {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserUtils.class);

    /**
     * 虚拟线程执行器（懒加载）
     * <p>
     * 使用 JDK 21 虚拟线程，适合 I/O 密集型任务
     * </p>
     */
    private static final class VirtualThreadExecutorHolder {
        static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 私有构造函数，防止实例化
     */
    private CurrentUserUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 用户上下文快照（线程安全） ====================

    /**
     * 用户上下文快照（不可变记录）
     * <p>
     * 使用 JDK 16+ Record 特性，创建不可变的用户信息快照。
     * 适用于需要在异步任务、虚拟线程中使用用户信息的场景。
     * </p>
     *
     * <p><b>线程安全性：</b></p>
     * <ul>
     *   <li>Record 是不可变的，天然线程安全</li>
     *   <li>捕获时机在主线程，使用时机在子线程</li>
     *   <li>避免了 SecurityContext 的线程绑定问题</li>
     * </ul>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * // 在主线程捕获
     * var snapshot = CurrentUserUtils.captureSnapshot();
     *
     * // 在任意线程使用
     * CompletableFuture.runAsync(() -> {
     *     Long userId = snapshot.userId();
     *     String username = snapshot.username();
     *     // ... 业务逻辑
     * });
     * </pre>
     *
     * @param userId        用户ID
     * @param username      用户账号
     * @param nickname      用户昵称
     * @param email         用户邮箱
     * @param phone         用户手机号
     * @param avatarUrl     用户头像URL
     * @param authenticated 是否已认证
     */
    public record UserSnapshot(
            Long userId,
            String username,
            String nickname,
            String email,
            String phone,
            String avatarUrl,
            boolean authenticated
    ) {
        /**
         * 空快照常量，表示未登录状态
         */
        public static final UserSnapshot EMPTY = new UserSnapshot(
                null, null, null, null, null, null, false
        );

        /**
         * 判断快照是否为空（未登录）
         *
         * @return true 表示未登录
         */
        public boolean isEmpty() {
            return userId == null;
        }

        /**
         * 判断快照是否有效（已登录）
         *
         * @return true 表示已登录
         */
        public boolean isPresent() {
            return userId != null;
        }

        /**
         * 如果用户已登录，执行指定操作
         *
         * @param action 要执行的操作
         */
        public void ifPresent(Consumer<UserSnapshot> action) {
            if (isPresent()) {
                action.accept(this);
            }
        }

        /**
         * 获取用户ID，如果未登录则返回默认值
         *
         * @param defaultValue 默认值
         * @return 用户ID或默认值
         */
        public Long userIdOrDefault(Long defaultValue) {
            return userId != null ? userId : defaultValue;
        }

        /**
         * 获取用户名，如果未登录则返回默认值
         *
         * @param defaultValue 默认值
         * @return 用户名或默认值
         */
        public String usernameOrDefault(String defaultValue) {
            return username != null ? username : defaultValue;
        }
    }

    /**
     * 捕获当前用户的上下文快照
     * <p>
     * 在主线程中调用此方法捕获用户信息，然后可以安全地在任意线程中使用。
     * 这是解决多线程/异步场景下获取用户信息的推荐方式。
     * </p>
     *
     * @return 用户上下文快照，如果未登录返回 {@link UserSnapshot#EMPTY}
     */
    public static UserSnapshot captureSnapshot() {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return UserSnapshot.EMPTY;
        }
        return new UserSnapshot(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.isAuthenticated()
        );
    }

    /**
     * 捕获当前用户的上下文快照（Optional 版本）
     *
     * @return Optional 包装的用户快照，如果未登录返回 empty
     */
    public static Optional<UserSnapshot> captureSnapshotOptional() {
        UserSnapshot snapshot = captureSnapshot();
        return snapshot.isPresent() ? Optional.of(snapshot) : Optional.empty();
    }

    // ==================== 基础访问方法 ====================

    /**
     * 获取当前登录用户对象
     * <p>
     * 从 Spring Security Context 中获取当前认证用户。
     * 如果用户未登录或 principal 不是 CurrentUser 类型，则返回 null。
     * </p>
     *
     * <p><b>JDK 21 优化：</b>使用 Pattern Matching for instanceof 简化代码</p>
     *
     * @return 当前登录用户对象，如果未登录或类型不匹配则返回 null
     */
    public static CurrentUser getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            // JDK 16+ Pattern Matching for instanceof
            if (authentication.getPrincipal() instanceof CurrentUser currentUser) {
                return currentUser;
            }

            return null;
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前登录用户对象（Optional 版本）
     * <p>
     * 推荐使用此方法进行空安全处理，避免 NullPointerException。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * // 链式调用
     * String nickname = CurrentUserUtils.findCurrentUser()
     *     .map(CurrentUser::getNickname)
     *     .orElse("游客");
     *
     * // 条件执行
     * CurrentUserUtils.findCurrentUser()
     *     .ifPresent(user -> log.info("当前用户: {}", user.getUsername()));
     * </pre>
     *
     * @return Optional 包装的当前用户对象
     */
    public static Optional<CurrentUser> findCurrentUser() {
        return Optional.ofNullable(getCurrentUser());
    }

    /**
     * 获取当前登录用户ID
     * <p>
     * 这是最常用的方法，用于获取当前用户的唯一标识符。
     * 通常用于审计字段填充、数据权限过滤等场景。
     * </p>
     *
     * @return 当前用户ID，如果未登录则返回 null
     */
    public static Long getUserId() {
        return findCurrentUser()
                .map(CurrentUser::getUserId)
                .orElse(null);
    }

    /**
     * 获取当前登录用户账号
     * <p>
     * 获取用户的登录账号（用户名）。
     * 通常用于日志记录、操作审计等场景。
     * </p>
     *
     * @return 当前用户账号，如果未登录则返回 null
     */
    public static String getUsername() {
        return findCurrentUser()
                .map(CurrentUser::getUsername)
                .orElse(null);
    }

    /**
     * 获取当前登录用户昵称
     * <p>
     * 获取用户的显示名称（昵称）。
     * 通常用于 UI 界面显示、通知消息等场景。
     * </p>
     *
     * @return 当前用户昵称，如果未登录则返回 null
     */
    public static String getNickname() {
        return findCurrentUser()
                .map(CurrentUser::getNickname)
                .orElse(null);
    }

    /**
     * 获取当前登录用户邮箱
     * <p>
     * 获取用户的电子邮箱地址。
     * 通常用于发送通知邮件、账号验证等场景。
     * </p>
     *
     * @return 当前用户邮箱，如果未设置则返回 null
     */
    public static String getEmail() {
        return findCurrentUser()
                .map(CurrentUser::getEmail)
                .orElse(null);
    }

    /**
     * 获取当前登录用户手机号
     * <p>
     * 获取用户的手机号码。
     * 通常用于发送短信通知、双因素认证等场景。
     * </p>
     *
     * @return 当前用户手机号，如果未设置则返回 null
     */
    public static String getPhone() {
        return findCurrentUser()
                .map(CurrentUser::getPhone)
                .orElse(null);
    }

    /**
     * 判断当前用户是否已认证
     * <p>
     * 检查用户是否已通过身份验证。
     * 注意：已认证不等于有权限，需要额外的权限检查。
     * </p>
     *
     * @return true 表示用户已认证，false 表示未认证
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return false;
            }

            // JDK 16+ Pattern Matching for instanceof
            if (authentication.getPrincipal() instanceof CurrentUser currentUser) {
                return currentUser.isAuthenticated();
            }

            return authentication.isAuthenticated();
        } catch (Exception e) {
            log.debug("检查用户认证状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前用户ID，如果未登录则返回指定的默认值
     * <p>
     * 提供默认值的版本，避免业务代码中频繁的 null 检查。
     * </p>
     *
     * @param defaultValue 默认值
     * @return 当前用户ID，如果未登录则返回 defaultValue
     */
    public static Long getUserIdOrDefault(Long defaultValue) {
        return findCurrentUser()
                .map(CurrentUser::getUserId)
                .orElse(defaultValue);
    }

    /**
     * 获取当前用户账号，如果未登录则返回指定的默认值
     * <p>
     * 提供默认值的版本，避免业务代码中频繁的 null 检查。
     * </p>
     *
     * @param defaultValue 默认值
     * @return 当前用户账号，如果未登录则返回 defaultValue
     */
    public static String getUsernameOrDefault(String defaultValue) {
        return findCurrentUser()
                .map(CurrentUser::getUsername)
                .orElse(defaultValue);
    }

    /**
     * 获取 Spring Security Authentication 对象
     * <p>
     * 直接访问底层的 Authentication 对象，用于需要完整认证信息的场景。
     * 通常不建议直接使用此方法，优先使用其他封装好的方法。
     * </p>
     *
     * @return Spring Security Authentication 对象，如果未认证则返回 null
     */
    public static Authentication getAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            log.debug("获取 Authentication 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取 Authentication 的 Optional 包装
     *
     * @return Optional 包装的 Authentication 对象
     */
    public static Optional<Authentication> findAuthentication() {
        return Optional.ofNullable(getAuthentication());
    }

    // ==================== SecurityContext 上下文传播方法 ====================

    /**
     * 包装 Runnable 以传播 SecurityContext
     * <p>
     * 在创建异步任务时使用，确保任务执行时能够访问到原始线程的安全上下文。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * ExecutorService executor = Executors.newFixedThreadPool(4);
     * executor.submit(CurrentUserUtils.wrapRunnable(() -> {
     *     // 这里可以正常获取用户信息
     *     Long userId = CurrentUserUtils.getUserId();
     * }));
     * </pre>
     *
     * @param task 原始任务
     * @return 包装后的任务，携带 SecurityContext
     */
    public static Runnable wrapRunnable(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");
        SecurityContext context = SecurityContextHolder.getContext();
        return () -> {
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                task.run();
            } finally {
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }

    /**
     * 包装 Callable 以传播 SecurityContext
     * <p>
     * 在创建有返回值的异步任务时使用。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * Future&lt;String&gt; future = executor.submit(
     *     CurrentUserUtils.wrapCallable(() -> CurrentUserUtils.getUsername())
     * );
     * </pre>
     *
     * @param task 原始任务
     * @param <T>  返回值类型
     * @return 包装后的任务，携带 SecurityContext
     */
    public static <T> Callable<T> wrapCallable(Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        SecurityContext context = SecurityContextHolder.getContext();
        return () -> {
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                return task.call();
            } finally {
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }

    /**
     * 包装 Supplier 以传播 SecurityContext
     *
     * @param supplier 原始 Supplier
     * @param <T>      返回值类型
     * @return 包装后的 Supplier，携带 SecurityContext
     */
    public static <T> Supplier<T> wrapSupplier(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        SecurityContext context = SecurityContextHolder.getContext();
        return () -> {
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                return supplier.get();
            } finally {
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }

    /**
     * 包装 Function 以传播 SecurityContext
     *
     * @param function 原始 Function
     * @param <T>      输入类型
     * @param <R>      返回类型
     * @return 包装后的 Function，携带 SecurityContext
     */
    public static <T, R> Function<T, R> wrapFunction(Function<T, R> function) {
        Objects.requireNonNull(function, "function must not be null");
        SecurityContext context = SecurityContextHolder.getContext();
        return (t) -> {
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                return function.apply(t);
            } finally {
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }

    /**
     * 包装 Consumer 以传播 SecurityContext
     *
     * @param consumer 原始 Consumer
     * @param <T>      输入类型
     * @return 包装后的 Consumer，携带 SecurityContext
     */
    public static <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        SecurityContext context = SecurityContextHolder.getContext();
        return (t) -> {
            SecurityContext previousContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                consumer.accept(t);
            } finally {
                SecurityContextHolder.setContext(previousContext);
            }
        };
    }

    // ==================== 虚拟线程支持（JDK 21） ====================

    /**
     * 在虚拟线程中执行任务并自动传播 SecurityContext
     * <p>
     * 使用 JDK 21 虚拟线程特性，适合 I/O 密集型的异步任务。
     * 虚拟线程相比平台线程更轻量，可以创建大量实例。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * CurrentUserUtils.runInVirtualThread(() -> {
     *     // 可以正常获取用户信息
     *     Long userId = CurrentUserUtils.getUserId();
     *     // 执行 I/O 操作...
     * });
     * </pre>
     *
     * @param task 要执行的任务
     * @return 虚拟线程对象，可用于 join 等待完成
     */
    public static Thread runInVirtualThread(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");
        return Thread.startVirtualThread(wrapRunnable(task));
    }

    /**
     * 在虚拟线程中执行任务并返回 CompletableFuture
     * <p>
     * 结合 JDK 21 虚拟线程和 CompletableFuture，提供便捷的异步编程模型。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * CompletableFuture&lt;Long&gt; future = CurrentUserUtils.supplyAsyncInVirtualThread(
     *     () -> CurrentUserUtils.getUserId()
     * );
     *
     * future.thenAccept(userId -> {
     *     System.out.println("User ID: " + userId);
     * });
     * </pre>
     *
     * @param supplier 要执行的任务
     * @param <T>      返回值类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsyncInVirtualThread(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return CompletableFuture.supplyAsync(
                wrapSupplier(supplier),
                VirtualThreadExecutorHolder.EXECUTOR
        );
    }

    /**
     * 在虚拟线程中执行无返回值任务并返回 CompletableFuture
     *
     * @param task 要执行的任务
     * @return CompletableFuture&lt;Void&gt;
     */
    public static CompletableFuture<Void> runAsyncInVirtualThread(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");
        return CompletableFuture.runAsync(
                wrapRunnable(task),
                VirtualThreadExecutorHolder.EXECUTOR
        );
    }

    // ==================== CompletableFuture 上下文传播 ====================

    /**
     * 使用默认 ForkJoinPool 执行异步任务，并传播 SecurityContext
     * <p>
     * 适用于 CPU 密集型任务，使用 ForkJoinPool.commonPool()。
     * </p>
     *
     * @param supplier 要执行的任务
     * @param <T>      返回值类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsyncWithContext(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return CompletableFuture.supplyAsync(wrapSupplier(supplier));
    }

    /**
     * 使用指定 Executor 执行异步任务，并传播 SecurityContext
     *
     * @param supplier 要执行的任务
     * @param executor 执行器
     * @param <T>      返回值类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsyncWithContext(Supplier<T> supplier, ExecutorService executor) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        return CompletableFuture.supplyAsync(wrapSupplier(supplier), executor);
    }

    /**
     * 使用默认 ForkJoinPool 执行无返回值异步任务，并传播 SecurityContext
     *
     * @param task 要执行的任务
     * @return CompletableFuture&lt;Void&gt;
     */
    public static CompletableFuture<Void> runAsyncWithContext(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");
        return CompletableFuture.runAsync(wrapRunnable(task));
    }

    /**
     * 使用指定 Executor 执行无返回值异步任务，并传播 SecurityContext
     *
     * @param task     要执行的任务
     * @param executor 执行器
     * @return CompletableFuture&lt;Void&gt;
     */
    public static CompletableFuture<Void> runAsyncWithContext(Runnable task, ExecutorService executor) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        return CompletableFuture.runAsync(wrapRunnable(task), executor);
    }

    // ==================== 在指定用户上下文中执行 ====================

    /**
     * 在指定的用户快照上下文中执行任务
     * <p>
     * 允许在任意线程中以指定用户身份执行操作，常用于系统任务或批处理。
     * 注意：这是一个特殊方法，请谨慎使用。
     * </p>
     *
     * @param snapshot 用户快照
     * @param action   要执行的操作
     * @deprecated 推荐直接使用 UserSnapshot 中的数据，而非依赖 SecurityContext
     */
    @Deprecated(since = "0.2.0", forRemoval = false)
    public static void executeWithSnapshot(UserSnapshot snapshot, Consumer<UserSnapshot> action) {
        Objects.requireNonNull(action, "action must not be null");
        if (snapshot != null && snapshot.isPresent()) {
            action.accept(snapshot);
        }
    }
}
