package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.UserProfile;

import java.util.List;
import java.util.Map;

/**
 * 用户档案 SPI 接口
 * <p>
 * 定义在 mortise-core，供社区、商城等业务模块注入使用，
 * 由 mortise-member 模块提供具体实现，避免业务模块间直接依赖。
 * </p>
 *
 * @author ronger
 */
public interface UserProfileProvider {

    /**
     * 查询单个用户档案
     *
     * @param userId 用户 ID
     * @return 用户档案，不存在时返回 null
     */
    UserProfile getUserProfile(Long userId);

    /**
     * 批量查询用户档案，供列表场景使用（避免 N+1）
     *
     * @param userIds 用户 ID 列表
     * @return userId → UserProfile 映射；不存在的 ID 不会出现在结果中
     */
    Map<Long, UserProfile> getUserProfiles(List<Long> userIds);
}
