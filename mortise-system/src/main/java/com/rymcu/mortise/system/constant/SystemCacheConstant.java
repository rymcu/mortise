package com.rymcu.mortise.system.constant;

/**
 * 系统业务模块缓存常量
 *
 * <p>包含用户、角色、权限、菜单、字典、系统配置等业务缓存</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public class SystemCacheConstant {

    // ==================== 用户相关缓存 ====================

    /**
     * 用户信息缓存
     */
    public static final String USER_INFO_CACHE = "user:info";
    public static final long USER_INFO_EXPIRE_HOURS = 1;

    /**
     * 用户详情缓存（包含关联数据）
     */
    public static final String USER_DETAIL_CACHE = "user:detail";
    public static final long USER_DETAIL_EXPIRE_HOURS = 2;

    /**
     * 用户权限缓存
     */
    public static final String USER_PERMISSIONS_CACHE = "user:permissions";
    public static final long USER_PERMISSIONS_EXPIRE_MINUTES = 30;

    /**
     * 用户角色缓存
     */
    public static final String USER_ROLES_CACHE = "user:roles";
    public static final long USER_ROLES_EXPIRE_HOURS = 1;

    /**
     * 用户会话缓存
     */
    public static final String USER_SESSION_CACHE = "user:session";
    public static final long USER_SESSION_EXPIRE_HOURS = 2;

    /**
     * 用户在线状态缓存
     */
    public static final String USER_ONLINE_STATUS_CACHE = "userOnlineStatus";
    public static final long USER_ONLINE_STATUS_EXPIRE_MINUTES = 30;

    /**
     * 临时数据缓存
     */
    public static final String TEMP_DATA_CACHE = "temp:data";
    public static final long TEMP_DATA_EXPIRE_MINUTES = 30;

    /**
     * 热点数据缓存
     */
    public static final String HOT_DATA_CACHE = "hot:data";
    public static final long HOT_DATA_EXPIRE_HOURS = 6;

    // ==================== 角色权限相关缓存 ====================

    /**
     * 角色信息缓存
     */
    public static final String ROLE_INFO_CACHE = "role:info";
    public static final long ROLE_INFO_EXPIRE_HOURS = 4;

    /**
     * 角色权限缓存
     */
    public static final String ROLE_PERMISSION_CACHE = "role:permission";
    public static final long ROLE_PERMISSION_EXPIRE_HOURS = 2;

    /**
     * 角色菜单缓存
     */
    public static final String ROLE_MENU_CACHE = "role:menu";
    public static final long ROLE_MENU_EXPIRE_HOURS = 4;

    /**
     * 权限信息缓存
     */
    public static final String PERMISSION_INFO_CACHE = "permission:info";
    public static final long PERMISSION_INFO_EXPIRE_HOURS = 6;

    // ==================== 菜单相关缓存 ====================

    /**
     * 菜单数据缓存
     */
    public static final String MENU_DATA_CACHE = "menu:data";
    public static final long MENU_DATA_EXPIRE_HOURS = 4;

    /**
     * 菜单树缓存
     */
    public static final String MENU_TREE_CACHE = "menu:tree";
    public static final long MENU_TREE_EXPIRE_HOURS = 4;

    /**
     * 用户菜单缓存
     */
    public static final String USER_MENU_CACHE = "user:menu";
    public static final long USER_MENU_EXPIRE_HOURS = 2;

    // ==================== 字典相关缓存 ====================

    /**
     * 字典数据缓存
     */
    public static final String DICT_DATA_CACHE = "dict:data";
    public static final long DICT_DATA_EXPIRE_HOURS = 12;

    /**
     * 字典类型缓存
     */
    public static final String DICT_TYPE_CACHE = "dict:type";
    public static final long DICT_TYPE_EXPIRE_HOURS = 12;

    /**
     * 字典项缓存（按类型）
     */
    public static final String DICT_ITEMS_CACHE = "dict:items";
    public static final long DICT_ITEMS_EXPIRE_HOURS = 12;

    // ==================== 系统配置缓存 ====================

    /**
     * 系统配置缓存
     */
    public static final String SYSTEM_CONFIG_CACHE = "system:config";
    public static final long SYSTEM_CONFIG_EXPIRE_HOURS = 6;

    /**
     * 系统参数缓存
     */
    public static final String SYSTEM_PARAM_CACHE = "system:param";
    public static final long SYSTEM_PARAM_EXPIRE_HOURS = 6;

    /**
     * 系统设置缓存
     */
    public static final String SYSTEM_SETTING_CACHE = "system:setting";
    public static final long SYSTEM_SETTING_EXPIRE_HOURS = 12;

    // ==================== 部门组织缓存 ====================

    /**
     * 部门信息缓存
     */
    public static final String DEPT_INFO_CACHE = "dept:info";
    public static final long DEPT_INFO_EXPIRE_HOURS = 4;

    /**
     * 部门树缓存
     */
    public static final String DEPT_TREE_CACHE = "dept:tree";
    public static final long DEPT_TREE_EXPIRE_HOURS = 4;

    /**
     * 组织架构缓存
     */
    public static final String ORG_STRUCTURE_CACHE = "org:structure";
    public static final long ORG_STRUCTURE_EXPIRE_HOURS = 6;

    // ==================== 操作日志缓存 ====================

    /**
     * 操作日志临时缓存（用于批量写入）
     */
    public static final String OPERATION_LOG_TEMP_CACHE = "log:operation-temp";
    public static final long OPERATION_LOG_TEMP_EXPIRE_MINUTES = 10;

    /**
     * 登录日志临时缓存
     */
    public static final String LOGIN_LOG_TEMP_CACHE = "log:login-temp";
    public static final long LOGIN_LOG_TEMP_EXPIRE_MINUTES = 10;

    // ==================== 认证相关缓存 ====================

    /**
     * 刷新令牌过期时间（天）
     */
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * 密码重置令牌过期时间（分钟）
     */
    public static final long PASSWORD_RESET_TOKEN_EXPIRE_MINUTES = 30;

    /**
     * 当前账号信息过期时间（小时）
     */
    public static final long CURRENT_ACCOUNT_EXPIRE_HOURS = 1;

    // ==================== 通用业务数据缓存 ====================

    /**
     * 热门数据缓存
     */
    public static final long HOT_DATA_EXPIRE_MINUTES = 15;

    /**
     * 统计数据缓存
     */
    public static final String STATISTICS_CACHE = "statistics:data";
    public static final long STATISTICS_EXPIRE_HOURS = 6;

    private SystemCacheConstant() {
        // 工具类禁止实例化
    }
}
