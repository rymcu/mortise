package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.core.model.UserBadgeAwardCommand;
import com.rymcu.mortise.core.model.UserBadgeDefinition;
import com.rymcu.mortise.core.model.UserBadgeEntry;
import com.rymcu.mortise.core.model.UserBadgeMetricCommand;
import com.rymcu.mortise.core.spi.UserBadgeProvider;
import com.rymcu.mortise.member.entity.MemberBadge;
import com.rymcu.mortise.member.entity.MemberUserBadge;
import com.rymcu.mortise.member.mapper.MemberBadgeMapper;
import com.rymcu.mortise.member.mapper.MemberUserBadgeMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rymcu.mortise.member.entity.table.MemberBadgeTableDef.MEMBER_BADGE;
import static com.rymcu.mortise.member.entity.table.MemberUserBadgeTableDef.MEMBER_USER_BADGE;

/**
 * 基于 member 模块的徽章提供者
 */
@Component
public class MemberBadgeProvider implements UserBadgeProvider {

    private final MemberBadgeMapper memberBadgeMapper;
    private final MemberUserBadgeMapper memberUserBadgeMapper;

    public MemberBadgeProvider(MemberBadgeMapper memberBadgeMapper,
                               MemberUserBadgeMapper memberUserBadgeMapper) {
        this.memberBadgeMapper = memberBadgeMapper;
        this.memberUserBadgeMapper = memberUserBadgeMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<UserBadgeEntry> awardBadge(UserBadgeAwardCommand command) {
        if (command == null || command.userId() == null || !StringUtils.hasText(command.badgeCode())) {
            return Optional.empty();
        }

        var badge = memberBadgeMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(MEMBER_BADGE.CODE.eq(command.badgeCode()))
                        .and(MEMBER_BADGE.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
        );
        if (badge == null) {
            return Optional.empty();
        }

        long exists = memberUserBadgeMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(MEMBER_USER_BADGE.USER_ID.eq(command.userId()))
                        .and(MEMBER_USER_BADGE.BADGE_ID.eq(badge.getId()))
                        .and(MEMBER_USER_BADGE.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
        );
        if (exists > 0) {
            return Optional.empty();
        }

        var now = LocalDateTime.now();
        var userBadge = new MemberUserBadge();
        userBadge.setUserId(command.userId());
        userBadge.setBadgeId(badge.getId());
        userBadge.setSourceType(command.sourceType());
        userBadge.setSourceId(command.sourceId());
        userBadge.setEarnedTime(now);
        userBadge.setCreatedTime(now);
        userBadge.setUpdatedTime(now);
        userBadge.setDelFlag(DelFlag.NORMAL.ordinal());
        memberUserBadgeMapper.insertSelective(userBadge);
        return Optional.of(toEntry(badge, userBadge.getEarnedTime()));
    }

    @Override
    public List<UserBadgeEntry> listUserBadges(Long userId, int limit) {
        if (userId == null) {
            return List.of();
        }
        var safeLimit = limit > 0 ? Math.min(limit, 24) : 12;
        var userBadges = memberUserBadgeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(MEMBER_USER_BADGE.USER_ID.eq(userId))
                        .and(MEMBER_USER_BADGE.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                        .orderBy(MEMBER_USER_BADGE.EARNED_TIME.desc(), MEMBER_USER_BADGE.ID.desc())
                        .limit(safeLimit)
        );
        if (userBadges.isEmpty()) {
            return List.of();
        }
        var badgeIds = userBadges.stream()
                .map(MemberUserBadge::getBadgeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        var badgeMap = memberBadgeMapper.selectListByQuery(
                        QueryWrapper.create()
                                .where(MEMBER_BADGE.ID.in(badgeIds))
                                .and(MEMBER_BADGE.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                ).stream()
                .collect(Collectors.toMap(MemberBadge::getId, Function.identity()));

        return userBadges.stream()
                .map(item -> {
                    var badge = badgeMap.get(item.getBadgeId());
                    return badge == null ? null : toEntry(badge, item.getEarnedTime());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserBadgeEntry> awardBadges(UserBadgeMetricCommand command) {
        if (command == null || command.userId() == null || command.metrics() == null || command.metrics().isEmpty()) {
            return List.of();
        }
        return listBadgeDefinitions().stream()
                .filter(definition -> StringUtils.hasText(definition.conditionType()))
                .filter(definition -> command.metrics().containsKey(definition.conditionType()))
                .filter(definition -> matchesMetric(definition, command.metrics().get(definition.conditionType())))
                .map(definition -> awardBadge(new UserBadgeAwardCommand(
                        command.userId(),
                        definition.code(),
                        command.sourceType(),
                        command.sourceId()
                )).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<UserBadgeDefinition> listBadgeDefinitions() {
        return memberBadgeMapper.selectListByQuery(
                        QueryWrapper.create()
                                .where(MEMBER_BADGE.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                                .orderBy(MEMBER_BADGE.ID.asc())
                ).stream()
                .map(item -> new UserBadgeDefinition(
                        item.getCode(),
                        item.getName(),
                        item.getIcon(),
                        item.getDescription(),
                        item.getConditionType(),
                        item.getConditionValue()
                ))
                .toList();
    }

    private boolean matchesMetric(UserBadgeDefinition definition, Long metricValue) {
        if (definition == null || metricValue == null || !StringUtils.hasText(definition.conditionValue())) {
            return false;
        }
        try {
            return metricValue >= Long.parseLong(definition.conditionValue().trim());
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private UserBadgeEntry toEntry(MemberBadge badge, LocalDateTime earnedTime) {
        return new UserBadgeEntry(
                badge.getCode(),
                badge.getName(),
                badge.getIcon(),
                badge.getDescription(),
                earnedTime
        );
    }
}
