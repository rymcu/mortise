package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.UserBadgeAwardCommand;
import com.rymcu.mortise.core.model.UserBadgeDefinition;
import com.rymcu.mortise.core.model.UserBadgeEntry;
import com.rymcu.mortise.core.model.UserBadgeMetricCommand;

import java.util.List;
import java.util.Optional;

/**
 * 用户徽章 SPI
 */
public interface UserBadgeProvider {

    /**
     * 发放徽章。
     *
     * @param command 发放命令
     * @return 若本次成功发放则返回徽章信息；已发放过时返回 empty
     */
    default Optional<UserBadgeEntry> awardBadge(UserBadgeAwardCommand command) {
        return Optional.empty();
    }

    /**
     * 查询用户徽章。
     *
     * @param userId 用户 ID
     * @param limit  返回上限
     * @return 徽章列表
     */
    default List<UserBadgeEntry> listUserBadges(Long userId, int limit) {
        return List.of();
    }

    /**
     * 按指标命令批量判定并发放徽章。
     *
     * @param command 徽章指标命令
     * @return 本次新授予的徽章列表
     */
    default List<UserBadgeEntry> awardBadges(UserBadgeMetricCommand command) {
        return List.of();
    }

    /**
     * 查询启用中的徽章定义。
     *
     * @return 徽章定义列表
     */
    default List<UserBadgeDefinition> listBadgeDefinitions() {
        return List.of();
    }
}
