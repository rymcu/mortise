package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.core.model.UserPointAwardCommand;
import com.rymcu.mortise.core.model.UserPointHistoryEntry;
import com.rymcu.mortise.core.model.UserPointSummary;
import com.rymcu.mortise.core.spi.UserPointProvider;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.entity.MemberPointHistory;
import com.rymcu.mortise.member.mapper.MemberPointHistoryMapper;
import com.rymcu.mortise.member.service.MemberService;
import com.rymcu.mortise.member.support.MemberPointLevelResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.rymcu.mortise.member.entity.table.MemberPointHistoryTableDef.MEMBER_POINT_HISTORY;
import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 基于 member 模块的积分提供者
 */
@Component
public class MemberPointProvider implements UserPointProvider {

    private final MemberService memberService;
    private final MemberPointHistoryMapper memberPointHistoryMapper;

    public MemberPointProvider(@Qualifier("memberServiceImpl") MemberService memberService,
                               MemberPointHistoryMapper memberPointHistoryMapper) {
        this.memberService = memberService;
        this.memberPointHistoryMapper = memberPointHistoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(UserPointAwardCommand command) {
        if (command == null
                || command.userId() == null
                || command.changeAmount() == null
                || command.changeAmount() <= 0
                || !StringUtils.hasText(command.bizKey())) {
            return false;
        }

        var duplicate = memberPointHistoryMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(MEMBER_POINT_HISTORY.USER_ID.eq(command.userId()))
                        .and(MEMBER_POINT_HISTORY.BIZ_KEY.eq(command.bizKey()))
        );
        if (duplicate > 0) {
            return false;
        }

        var member = memberService.getOne(
                QueryWrapper.create()
                        .select(MEMBER.ID, MEMBER.POINTS)
                        .where(MEMBER.ID.eq(command.userId()))
        );
        if (member == null) {
            return false;
        }

        int currentPoints = member.getPoints() != null ? member.getPoints() : 0;
        int updatedPoints = currentPoints + command.changeAmount();
        var levelLabel = MemberPointLevelResolver.resolveLabel(updatedPoints);
        var now = LocalDateTime.now();

        var update = UpdateEntity.of(Member.class, member.getId());
        update.setPoints(updatedPoints);
        update.setMemberLevel(levelLabel);
        update.setUpdatedTime(now);
        memberService.updateById(update);

        var history = new MemberPointHistory();
        history.setUserId(command.userId());
        history.setChangeAmount(command.changeAmount());
        history.setCurrentPoints(updatedPoints);
        history.setBizType(command.bizType());
        history.setBizKey(command.bizKey());
        history.setBizId(command.bizId());
        history.setReason(command.reason());
        history.setCreatedTime(now);
        history.setUpdatedTime(now);
        history.setDelFlag(DelFlag.NORMAL.ordinal());
        memberPointHistoryMapper.insertSelective(history);
        return true;
    }

    @Override
    public UserPointSummary getPointSummary(Long userId) {
        if (userId == null) {
            return new UserPointSummary(null, 0, MemberPointLevelResolver.resolveLabel(0));
        }
        var member = memberService.getOne(
                QueryWrapper.create()
                        .select(MEMBER.ID, MEMBER.POINTS)
                        .where(MEMBER.ID.eq(userId))
        );
        if (member == null) {
            return new UserPointSummary(userId, 0, MemberPointLevelResolver.resolveLabel(0));
        }
        int points = member.getPoints() != null ? member.getPoints() : 0;
        return new UserPointSummary(userId, points, MemberPointLevelResolver.resolveLabel(points));
    }

    @Override
    public List<UserPointHistoryEntry> listPointHistories(Long userId, int limit) {
        if (userId == null) {
            return List.of();
        }
        var safeLimit = limit > 0 ? Math.min(limit, 50) : 20;
        return memberPointHistoryMapper.selectListByQuery(
                        QueryWrapper.create()
                                .select(MEMBER_POINT_HISTORY.ALL_COLUMNS)
                                .where(MEMBER_POINT_HISTORY.USER_ID.eq(userId))
                                .orderBy(MEMBER_POINT_HISTORY.CREATED_TIME.desc(), MEMBER_POINT_HISTORY.ID.desc())
                                .limit(safeLimit)
                ).stream()
                .map(item -> new UserPointHistoryEntry(
                        item.getId(),
                        item.getChangeAmount(),
                        item.getCurrentPoints(),
                        item.getReason(),
                        item.getBizType(),
                        item.getBizId(),
                        MemberPointLevelResolver.resolveLabel(item.getCurrentPoints()),
                        item.getCreatedTime()
                ))
                .toList();
    }
}
