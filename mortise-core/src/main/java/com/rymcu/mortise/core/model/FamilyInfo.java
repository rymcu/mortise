package com.rymcu.mortise.core.model;

/**
 * 家庭摘要模型。
 *
 * @param id 家庭 ID
 * @param familyName 家庭名称
 * @param description 家庭描述
 * @param ownerMemberId 家庭创建者/管理员会员 ID
 * @param memberCount 家庭成员数
 */
public record FamilyInfo(
        Long id,
        String familyName,
        String description,
        Long ownerMemberId,
        int memberCount
) {
}
