package com.rymcu.mortise.cache.spi;

import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.util.Map;

/**
 * 缓存配置扩展接口 (SPI)
 * 业务模块可实现此接口来注册自己的缓存策略
 * 
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class SystemCacheConfigurer implements CacheConfigurer {
 *     &#64;Override
 *     public Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig) {
 *         Map<String, RedisCacheConfiguration> configs = new HashMap<>();
 *         // 用户缓存 - 1小时
 *         configs.put(CacheConstant.USER_INFO_CACHE, 
 *                    defaultConfig.entryTtl(Duration.ofHours(1)));
 *         return configs;
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface CacheConfigurer {

    /**
     * 获取配置优先级，数字越小优先级越高
     * 默认优先级为 100
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 配置缓存策略
     * 
     * @param defaultConfig 默认缓存配置，可基于此配置进行修改
     * @return 缓存名称 -> 缓存配置映射表
     */
    Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig);
}
