package com.rymcu.mortise.core.constant;

/**
 * 缓存相关常量
 *
 * @author ronger
 */
public final class CacheConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private CacheConstant() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // === 缓存前缀常量 ===
    /**
     * 缓存名称前缀
     */
    public static final String CACHE_NAME_PREFIX = "mortise:cache:";

    // === 缓存名称常量 ===
    /**
     * 用户信息缓存名称
     */
    public static final String USER_INFO_CACHE = "userInfo";

    /**
     * 用户会话缓存名称
     */
    public static final String USER_SESSION_CACHE = "userSession";

    /**
     * 用户权限缓存名称
     */
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";

    /**
     * 角色权限缓存名称
     */
    public static final String ROLE_PERMISSION_CACHE = "rolePermission";

    /**
     * 菜单数据缓存名称
     */
    public static final String MENU_DATA_CACHE = "menuData";

    /**
     * 字典数据缓存名称
     */
    public static final String DICT_DATA_CACHE = "dictData";

    /**
     * 系统配置缓存名称
     */
    public static final String SYSTEM_CONFIG_CACHE = "systemConfig";

    /**
     * 热点数据缓存名称
     */
    public static final String HOT_DATA_CACHE = "hotData";

    /**
     * 统计数据缓存名称
     */
    public static final String STATISTICS_CACHE = "statistics";

    /**
     * 临时数据缓存名称
     */
    public static final String TEMP_DATA_CACHE = "tempData";

    // === 缓存过期时间常量（分钟） ===
    /**
     * 默认缓存过期时间 - 30分钟
     */
    public static final int DEFAULT_EXPIRE_MINUTES = 30;

    /**
     * 临时数据过期时间 - 5分钟
     */
    public static final int TEMP_DATA_EXPIRE_MINUTES = 5;

    /**
     * 热点数据过期时间 - 15分钟
     */
    public static final int HOT_DATA_EXPIRE_MINUTES = 15;

    /**
     * 用户权限过期时间 - 30分钟
     */
    public static final int USER_PERMISSIONS_EXPIRE_MINUTES = 30;

    // === 缓存过期时间常量（小时） ===
    /**
     * 用户信息过期时间 - 1小时
     */
    public static final int USER_INFO_EXPIRE_HOURS = 1;

    /**
     * 用户会话过期时间 - 2小时
     */
    public static final int USER_SESSION_EXPIRE_HOURS = 2;

    /**
     * 角色权限过期时间 - 2小时
     */
    public static final int ROLE_PERMISSION_EXPIRE_HOURS = 2;

    /**
     * 菜单数据过期时间 - 4小时
     */
    public static final int MENU_DATA_EXPIRE_HOURS = 4;

    /**
     * 系统配置过期时间 - 6小时
     */
    public static final int SYSTEM_CONFIG_EXPIRE_HOURS = 6;

    /**
     * 字典数据过期时间 - 12小时
     */
    public static final int DICT_DATA_EXPIRE_HOURS = 12;

    /**
     * 统计数据过期时间 - 1小时
     */
    public static final int STATISTICS_EXPIRE_HOURS = 1;
}