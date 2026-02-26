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
     * 账号序列缓存（用于防止并发登录）
     */
    public static final String ACCOUNT_SEQUENCE_CACHE = "account:sequence";
    public static final long ACCOUNT_SEQUENCE_EXPIRE_HOURS = 8760; // 1年

    // ==================== 登录安全缓存 ====================

    /**
     * 登录失败次数缓存（用于限制登录尝试）
     */
    public static final String LOGIN_FAIL_COUNT_CACHE = "login:fail-count";
    public static final long LOGIN_FAIL_COUNT_EXPIRE_MINUTES = 30;

    /**
     * 账号锁定缓存
     */
    public static final String ACCOUNT_LOCK_CACHE = "account:lock";
    public static final long ACCOUNT_LOCK_EXPIRE_HOURS = 1;

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

    /**
     * 字典选项缓存
     */
    public static final String DICT_OPTIONS_CACHE = "dict:options";
    public static final long DICT_OPTIONS_EXPIRE_HOURS = 12;

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
     * 刷新令牌缓存
     */
    public static final String REFRESH_TOKEN_CACHE = "auth:refresh_token";
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * 密码重置令牌缓存
     */
    public static final String PASSWORD_RESET_TOKEN_CACHE = "auth:reset_token";
    public static final long PASSWORD_RESET_TOKEN_EXPIRE_MINUTES = 30;

    /**
     * 当前账号缓存
     */
    public static final String CURRENT_ACCOUNT_CACHE = "auth:current_account";
    public static final long CURRENT_ACCOUNT_EXPIRE_HOURS = 1;

    /**
     * 验证码缓存
     */
    public static final String VERIFICATION_CODE_CACHE = "verification:code";
    public static final long VERIFICATION_CODE_EXPIRE_MINUTES = 5;

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

    // ==================== Dashboard 统计缓存 ====================

    /**
     * 用户数统计缓存
     */
    public static final String DASHBOARD_USER_COUNT = "dashboard:user:count";

    /**
     * 角色数统计缓存
     */
    public static final String DASHBOARD_ROLE_COUNT = "dashboard:role:count";

    /**
     * 菜单数统计缓存
     */
    public static final String DASHBOARD_MENU_COUNT = "dashboard:menu:count";

    /**
     * 会员数统计缓存
     */
    public static final String DASHBOARD_MEMBER_COUNT = "dashboard:member:count";

    /**
     * Dashboard 统计缓存过期时间（小时）
     */
    public static final long DASHBOARD_STATS_EXPIRE_HOURS = 1;

    /**
     * OAuth2 临时用户信息
     */
    public static final String STANDARD_OAUTH2_USER_INFO = "auth:standard:userinfo";
    public static final long STANDARD_OAUTH2_USER_INFO_EXPIRE_MINUTES = 30;

    /**
     * TokenUser 临时信息
     */
    public static final String STANDARD_AUTH_TOKEN_USER = "auth:standard:auth_token_user";
    public static final long STANDARD_AUTH_TOKEN_USER_EXPIRE_MINUTES = 5;

    private SystemCacheConstant() {
        // 工具类禁止实例化
    }
}
