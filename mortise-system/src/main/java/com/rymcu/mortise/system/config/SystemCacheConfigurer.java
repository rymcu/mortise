package com.rymcu.mortise.system.config;

import com.rymcu.mortise.cache.spi.CacheConfigurer;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统模块缓存配置器
 *
 * <p>实现 CacheConfigurer SPI，为系统业务相关的缓存提供配置</p>
 *
 * <p><strong>配置的缓存：</strong></p>
 * <ul>
 *     <li>用户信息缓存</li>
 *     <li>用户权限缓存</li>
 *     <li>角色权限缓存</li>
 *     <li>菜单数据缓存</li>
 *     <li>字典数据缓存</li>
 *     <li>系统配置缓存</li>
 *     <li>通用业务数据缓存</li>
 * </ul>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class SystemCacheConfigurer implements CacheConfigurer {

    @Override
    public int getOrder() {
        return 100; // 默认优先级
    }

    @Override
    public Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        // === 用户相关缓存 ===
        configs.put(SystemCacheConstant.USER_INFO_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.USER_INFO_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.USER_DETAIL_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.USER_DETAIL_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.USER_PERMISSIONS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(SystemCacheConstant.USER_PERMISSIONS_EXPIRE_MINUTES)));

        configs.put(SystemCacheConstant.USER_ROLES_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.USER_ROLES_EXPIRE_HOURS)));

        // === 角色权限相关缓存 ===
        configs.put(SystemCacheConstant.ROLE_INFO_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.ROLE_INFO_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.ROLE_PERMISSION_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.ROLE_PERMISSION_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.ROLE_MENU_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.ROLE_MENU_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.PERMISSION_INFO_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.PERMISSION_INFO_EXPIRE_HOURS)));

        // === 菜单相关缓存 ===
        configs.put(SystemCacheConstant.MENU_DATA_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.MENU_DATA_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.MENU_TREE_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.MENU_TREE_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.USER_MENU_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.USER_MENU_EXPIRE_HOURS)));

        // === 字典相关缓存 ===
        configs.put(SystemCacheConstant.DICT_DATA_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.DICT_DATA_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.DICT_TYPE_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.DICT_TYPE_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.DICT_ITEMS_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.DICT_ITEMS_EXPIRE_HOURS)));

        // === 系统配置缓存 ===
        configs.put(SystemCacheConstant.SYSTEM_CONFIG_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.SYSTEM_CONFIG_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.SYSTEM_PARAM_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.SYSTEM_PARAM_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.SYSTEM_SETTING_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.SYSTEM_SETTING_EXPIRE_HOURS)));

        // === 通用业务数据缓存 ===
        configs.put(SystemCacheConstant.HOT_DATA_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(SystemCacheConstant.HOT_DATA_EXPIRE_MINUTES)));

        configs.put(SystemCacheConstant.STATISTICS_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(SystemCacheConstant.STATISTICS_EXPIRE_HOURS)));

        configs.put(SystemCacheConstant.TEMP_DATA_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(SystemCacheConstant.TEMP_DATA_EXPIRE_MINUTES)));

        log.info("系统缓存配置已加载: {} 个缓存策略", configs.size());

        return configs;
    }
}
