package com.rymcu.mortise.cache.constant;

/**
 * 缓存基础常量定义
 * 
 * <p>本类仅包含通用的缓存配置常量，业务相关的缓存常量已拆分到各业务模块：</p>
 * <ul>
 *     <li>{@code mortise-auth}: 认证授权相关缓存 - {@code AuthCacheConstant}</li>
 *     <li>{@code mortise-system}: 系统业务相关缓存 - {@code SystemCacheConstant}</li>
 *     <li>{@code mortise-core}: 核心领域相关缓存 - {@code CoreCacheConstant}</li>
 * </ul>
 *
 * @author ronger
 */
public class CacheConstant {

    /**
     * 缓存名称前缀
     */
    public static final String CACHE_NAME_PREFIX = "mortise:";

    /**
     * 默认过期时间（分钟）
     */
    public static final long DEFAULT_EXPIRE_MINUTES = 30;

    /**
     * 默认过期时间（小时）
     */
    public static final long DEFAULT_EXPIRE_HOURS = 2;

    /**
     * 默认过期时间（天）
     */
    public static final long DEFAULT_EXPIRE_DAYS = 1;

    // ==================== 通用业务数据缓存 ====================

    /**
     * 热点数据缓存
     */
    public static final String HOT_DATA_CACHE = "hot:data";
    public static final long HOT_DATA_EXPIRE_MINUTES = 15;

    /**
     * 统计数据缓存
     */
    public static final String STATISTICS_CACHE = "statistics";
    public static final long STATISTICS_EXPIRE_HOURS = 1;

    /**
     * 临时数据缓存
     */
    public static final String TEMP_DATA_CACHE = "temp:data";
    public static final long TEMP_DATA_EXPIRE_MINUTES = 5;

    private CacheConstant() {
        // 工具类禁止实例化
    }
}
