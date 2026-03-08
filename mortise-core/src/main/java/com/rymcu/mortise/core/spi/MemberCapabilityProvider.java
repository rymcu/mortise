package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.MemberCapability;

import java.util.Optional;

/**
 * 会员能力 SPI
 * <p>
 * 为业务模块提供会员资格判断，避免将资格语义混入公开档案模型。
 * </p>
 */
public interface MemberCapabilityProvider {

    /**
     * 查询用户会员能力快照
     *
     * @param userId 用户 ID
     * @return 会员能力快照
     */
    Optional<MemberCapability> getMemberCapability(Long userId);

    /**
     * 判断用户是否具备有效会员资格
     *
     * @param userId 用户 ID
     * @return 是否具备会员资格
     */
    default boolean hasActiveMembership(Long userId) {
        return getMemberCapability(userId)
                .map(MemberCapability::active)
                .orElse(false);
    }
}