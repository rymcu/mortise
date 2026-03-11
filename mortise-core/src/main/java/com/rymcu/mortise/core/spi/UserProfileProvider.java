package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.UserProfile;
import com.rymcu.mortise.core.model.UserLeaderboardEntry;

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

    /**
     * 按关键词搜索用户档案，供协作者选择、@ 提及等轻量候选场景复用。
     *
     * @param keyword 搜索关键词
     * @param limit 返回条数上限
     * @return 用户档案列表
     */
    default List<UserProfile> searchUserProfiles(String keyword, int limit) {
        return List.of();
    }

    /**
     * 查询积分排行榜快照，供社区读侧展示使用。
     *
     * @param limit 返回条数上限
     * @return 排行榜条目
     */
    default List<UserLeaderboardEntry> listLeaderboardEntries(int limit) {
        return List.of();
    }
}
