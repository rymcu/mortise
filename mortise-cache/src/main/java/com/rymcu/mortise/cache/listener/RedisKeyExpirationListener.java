package com.rymcu.mortise.cache.listener;

import com.rymcu.mortise.cache.spi.CacheExpirationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Redis 键过期事件监听器
 * 基于 SPI 机制处理缓存失效事件
 * 
 * <p>使用方式：</p>
 * <ol>
 *     <li>业务模块实现 {@link CacheExpirationHandler} 接口</li>
 *     <li>注册为 Spring Bean (@Component)</li>
 *     <li>监听器会自动发现并调用相应的处理器</li>
 * </ol>
 * 
 * @author ronger
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final List<CacheExpirationHandler> expirationHandlers;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                    Optional<List<CacheExpirationHandler>> handlersOptional) {
        super(listenerContainer);
        this.expirationHandlers = handlersOptional.orElse(List.of());
        log.info("Redis键过期监听器初始化完成，发现 {} 个缓存失效处理器", expirationHandlers.size());
        
        // 打印所有注册的处理器信息
        expirationHandlers.forEach(handler -> 
            log.info("注册缓存失效处理器: {} (优先级: {}, 启用: {})", 
                handler.getName(), handler.getOrder(), handler.isEnabled()));
    }

    /**
     * 处理 Redis 键过期事件
     * 按优先级顺序调用所有支持的处理器
     * 
     * @param message Redis 过期事件消息
     * @param pattern 匹配模式
     */
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("检测到Redis键过期事件：{}", expiredKey);

        try {
            // 查找支持处理该键的处理器
            List<CacheExpirationHandler> supportedHandlers = expirationHandlers.stream()
                    .filter(CacheExpirationHandler::isEnabled)
                    .filter(handler -> handler.supports(expiredKey))
                    .sorted(Comparator.comparingInt(CacheExpirationHandler::getOrder))
                    .toList();

            if (supportedHandlers.isEmpty()) {
                log.debug("没有找到支持处理键 {} 的处理器", expiredKey);
                return;
            }

            // 依次调用所有支持的处理器
            for (CacheExpirationHandler handler : supportedHandlers) {
                try {
                    log.debug("使用处理器 {} 处理键过期事件：{}", handler.getName(), expiredKey);
                    handler.handle(expiredKey);
                    log.debug("处理器 {} 成功处理键过期事件：{}", handler.getName(), expiredKey);
                } catch (Exception e) {
                    log.error("处理器 {} 处理键过期事件失败：{}", handler.getName(), expiredKey, e);
                    // 继续处理其他处理器，不因一个处理器失败而中断
                }
            }

        } catch (Exception e) {
            log.error("处理Redis键过期事件时发生异常：{}", expiredKey, e);
        }

        // 调用父类方法，确保框架默认行为正常执行
        super.onMessage(message, pattern);
    }
}