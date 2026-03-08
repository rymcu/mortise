package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.MemberContact;

import java.util.Optional;

/**
 * 会员联系信息 SPI
 * <p>
 * 为业务模块提供已验证联系方式，避免把敏感联系信息塞入公开档案模型。
 * </p>
 */
public interface MemberContactProvider {

    /**
     * 查询用户联系信息快照
     *
     * @param userId 用户 ID
     * @return 联系信息快照
     */
    Optional<MemberContact> getMemberContact(Long userId);

    /**
     * 查询用户已验证邮箱
     *
     * @param userId 用户 ID
     * @return 已验证邮箱，不存在时为空
     */
    default Optional<String> getVerifiedEmail(Long userId) {
        return getMemberContact(userId)
                .filter(MemberContact::emailVerified)
                .map(MemberContact::email)
                .filter(email -> email != null && !email.isBlank());
    }
}