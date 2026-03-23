package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.FamilyInfo;

import java.util.List;
import java.util.Optional;

/**
 * 家庭查询 SPI。
 * <p>
 * 由 member 模块提供实现，供 AIoT、OTA 等业务模块复用家庭归属与管理员校验能力，
 * 避免业务模块之间形成同层直接依赖。
 * </p>
 */
public interface FamilyQueryProvider {

    /**
     * 查询家庭摘要。
     *
     * @param familyId 家庭 ID
     * @return 家庭摘要
     */
    Optional<FamilyInfo> getFamily(Long familyId);

    /**
     * 查询会员加入的家庭列表。
     *
     * @param memberId 会员 ID
     * @return 家庭摘要列表
     */
    List<FamilyInfo> listMemberFamilies(Long memberId);

    /**
     * 查询会员当前家庭。
     *
     * @param memberId 会员 ID
     * @return 当前家庭摘要
     */
    Optional<FamilyInfo> getCurrentFamily(Long memberId);

    /**
     * 判断会员是否属于某个家庭。
     *
     * @param memberId 会员 ID
     * @param familyId 家庭 ID
     * @return 是否属于该家庭
     */
    boolean isFamilyMember(Long memberId, Long familyId);

    /**
     * 判断会员是否是家庭管理员。
     *
     * @param memberId 会员 ID
     * @param familyId 家庭 ID
     * @return 是否为管理员
     */
    boolean isFamilyAdmin(Long memberId, Long familyId);
}
