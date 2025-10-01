package com.rymcu.mortise.core.constant;

/**
 * 核心领域模块缓存常量
 * 
 * <p>包含领域模型、业务规则等核心缓存配置</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public class CoreCacheConstant {

    // ==================== 领域对象缓存 ====================

    /**
     * 领域实体缓存
     */
    public static final String DOMAIN_ENTITY_CACHE = "domain:entity";
    public static final long DOMAIN_ENTITY_EXPIRE_HOURS = 2;

    /**
     * 聚合根缓存
     */
    public static final String AGGREGATE_ROOT_CACHE = "domain:aggregate";
    public static final long AGGREGATE_ROOT_EXPIRE_HOURS = 1;

    // ==================== 业务规则缓存 ====================

    /**
     * 业务规则缓存
     */
    public static final String BUSINESS_RULE_CACHE = "business:rule";
    public static final long BUSINESS_RULE_EXPIRE_HOURS = 6;

    /**
     * 验证规则缓存
     */
    public static final String VALIDATION_RULE_CACHE = "validation:rule";
    public static final long VALIDATION_RULE_EXPIRE_HOURS = 4;

    // ==================== 枚举字典缓存 ====================

    /**
     * 枚举值缓存
     */
    public static final String ENUM_VALUES_CACHE = "enum:values";
    public static final long ENUM_VALUES_EXPIRE_HOURS = 24;

    /**
     * 常量映射缓存
     */
    public static final String CONSTANT_MAPPING_CACHE = "constant:mapping";
    public static final long CONSTANT_MAPPING_EXPIRE_HOURS = 12;

    // ==================== 事件缓存 ====================

    /**
     * 领域事件临时缓存
     */
    public static final String DOMAIN_EVENT_TEMP_CACHE = "event:domain-temp";
    public static final long DOMAIN_EVENT_TEMP_EXPIRE_MINUTES = 30;

    /**
     * 事件处理状态缓存
     */
    public static final String EVENT_PROCESS_STATUS_CACHE = "event:process-status";
    public static final long EVENT_PROCESS_STATUS_EXPIRE_HOURS = 1;

    private CoreCacheConstant() {
        // 工具类禁止实例化
    }
}
