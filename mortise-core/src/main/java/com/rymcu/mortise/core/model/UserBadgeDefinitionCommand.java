package com.rymcu.mortise.core.model;

/**
 * 徽章定义维护命令
 *
 * @param code        徽章编码
 * @param name        徽章名称
 * @param icon        徽章图标
 * @param description 徽章描述
 */
public record UserBadgeDefinitionCommand(
        String code,
        String name,
        String icon,
        String description
) {
}
