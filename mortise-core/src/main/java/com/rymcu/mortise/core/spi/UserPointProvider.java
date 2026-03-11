package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.UserPointAwardCommand;
import com.rymcu.mortise.core.model.UserPointHistoryEntry;
import com.rymcu.mortise.core.model.UserPointSummary;

import java.util.List;

/**
 * 用户积分 SPI
 * <p>
 * 供社区等业务模块读写积分，但具体积分持久化与等级映射由 member 模块实现，
 * 避免业务模块直接依赖会员实现细节。
 * </p>
 */
public interface UserPointProvider {

    /**
     * 增加用户积分。
     *
     * @param command 变更命令
     * @return 是否执行成功；若被幂等去重则返回 false
     */
    default boolean addPoints(UserPointAwardCommand command) {
        return false;
    }

    /**
     * 查询用户积分摘要。
     *
     * @param userId 用户 ID
     * @return 积分摘要
     */
    default UserPointSummary getPointSummary(Long userId) {
        return new UserPointSummary(userId, 0, "Lv.1 新手");
    }

    /**
     * 查询积分历史。
     *
     * @param userId 用户 ID
     * @param limit  返回上限
     * @return 历史列表
     */
    default List<UserPointHistoryEntry> listPointHistories(Long userId, int limit) {
        return List.of();
    }
}
