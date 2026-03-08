package com.rymcu.mortise.core.model;

/**
 * 会员联系信息快照
 *
 * @param userId        用户 ID
 * @param email         邮箱地址
 * @param emailVerified 邮箱是否已验证
 */
public record MemberContact(
        Long userId,
        String email,
        boolean emailVerified
) {
}