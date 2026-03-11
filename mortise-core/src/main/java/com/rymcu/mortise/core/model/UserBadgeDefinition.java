package com.rymcu.mortise.core.model;

/**
 * 徽章定义
 *
 * @param code           徽章编码
 * @param name           徽章名称
 * @param icon           徽章图标
 * @param description    徽章描述
 * @param conditionType  条件类型
 * @param conditionValue 条件值
 */
public record UserBadgeDefinition(
        String code,
        String name,
        String icon,
        String description,
        String conditionType,
        String conditionValue
) {
}
