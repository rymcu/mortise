package com.rymcu.mortise.core.spi;

import java.util.Map;

/**
 * 系统配置存储 SPI 接口
 * <p>
 * 提供基于分组的键值对配置存储能力，业务模块通过此接口
 * 读写系统配置，而无需直接依赖具体的持久化实现。
 * </p>
 *
 * @author ronger
 */
public interface SystemConfigStorage {

    /**
     * 加载指定分组的所有配置项
     *
     * @param group 配置分组标识
     * @return 配置键值对（key → value），不存在时返回空 Map
     */
    Map<String, String> loadGroupValues(String group);

    /**
     * 新增或更新单条配置项
     *
     * @param group 配置分组标识
     * @param key   配置键
     * @param value 配置值
     */
    void upsertValue(String group, String key, String value);
}
