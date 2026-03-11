package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.common.enumerate.DelFlag;
import com.rymcu.mortise.core.model.UserLeaderboardEntry;
import com.rymcu.mortise.core.model.UserProfile;
import com.rymcu.mortise.core.spi.UserProfileProvider;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.service.MemberService;
import com.rymcu.mortise.member.support.MemberPointLevelResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 基于 member 数据的用户档案提供者
 * <p>
 * 实现 {@link UserProfileProvider} SPI，将 {@link Member} 数据（nickname、avatarUrl）
 * 和 profile JSONB 扩展字段（bio、website 等社交链接）组合为统一的 {@link UserProfile}，
 * 供社区等业务模块跨模块查询用户展示信息。
 * </p>
 *
 * @author ronger
 */
@Component
public class MemberUserProfileProvider implements UserProfileProvider {

    private final MemberService memberService;

    public MemberUserProfileProvider(@Qualifier("memberServiceImpl") MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public UserProfile getUserProfile(Long userId) {
        var member = memberService.getOne(
                QueryWrapper.create()
                        .select(MEMBER.ID, MEMBER.NICKNAME, MEMBER.AVATAR_URL, MEMBER.PROFILE)
                        .where(MEMBER.ID.eq(userId))
                        .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
        );
        return member == null ? null : toUserProfile(member);
    }

    @Override
    public Map<Long, UserProfile> getUserProfiles(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberService.list(
                        QueryWrapper.create()
                                .select(MEMBER.ID, MEMBER.NICKNAME, MEMBER.AVATAR_URL, MEMBER.PROFILE)
                                .where(MEMBER.ID.in(userIds))
                                .and(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                ).stream()
                .collect(Collectors.toMap(Member::getId, this::toUserProfile, (a, b) -> a));
    }

    @Override
    public List<UserProfile> searchUserProfiles(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        var normalizedKeyword = keyword.trim();
        var safeLimit = limit > 0 ? Math.min(limit, 20) : 8;
        var query = QueryWrapper.create()
                .select(MEMBER.ID, MEMBER.NICKNAME, MEMBER.AVATAR_URL, MEMBER.PROFILE)
                .where(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                .and(MEMBER.STATUS.eq(0));
        if (normalizedKeyword.chars().allMatch(Character::isDigit)) {
            query.and(MEMBER.ID.eq(Long.parseLong(normalizedKeyword))
                    .or(MEMBER.NICKNAME.like(normalizedKeyword))
                    .or(MEMBER.USERNAME.like(normalizedKeyword))
                    .or(MEMBER.EMAIL.like(normalizedKeyword))
                    .or(MEMBER.PHONE.like(normalizedKeyword)));
        } else {
            query.and(MEMBER.NICKNAME.like(normalizedKeyword)
                    .or(MEMBER.USERNAME.like(normalizedKeyword))
                    .or(MEMBER.EMAIL.like(normalizedKeyword))
                    .or(MEMBER.PHONE.like(normalizedKeyword)));
        }
        return memberService.list(query
                        .orderBy(MEMBER.ID.desc())
                        .limit(safeLimit))
                .stream()
                .map(this::toUserProfile)
                .toList();
    }

    @Override
    public List<UserLeaderboardEntry> listLeaderboardEntries(int limit) {
        var safeLimit = limit > 0 ? Math.min(limit, 50) : 20;
        return memberService.list(
                        QueryWrapper.create()
                                .select(MEMBER.ID, MEMBER.NICKNAME, MEMBER.AVATAR_URL, MEMBER.POINTS, MEMBER.MEMBER_LEVEL)
                                .where(MEMBER.DEL_FLAG.eq(DelFlag.NORMAL.ordinal()))
                                .and(MEMBER.STATUS.eq(0))
                                .and(MEMBER.POINTS.isNotNull())
                                .orderBy(MEMBER.POINTS.desc(), MEMBER.UPDATED_TIME.asc(), MEMBER.ID.asc())
                                .limit(safeLimit)
                ).stream()
                .map(member -> new UserLeaderboardEntry(
                        member.getId(),
                        member.getNickname(),
                        member.getAvatarUrl(),
                        member.getPoints(),
                        MemberPointLevelResolver.resolveLabel(member.getPoints())
                ))
                .toList();
    }

    /**
     * 将 Member 实体转换为 UserProfile VO。
     * bio、website、location、github、weibo、wechat、qq 等扩展字段存储于 profile JSONB 列。
     */
    private UserProfile toUserProfile(Member member) {
        var ext = member.getProfile();
        return new UserProfile(
                member.getId(),
                member.getNickname(),
                member.getAvatarUrl(),
                ext != null ? (String) ext.get("bio") : null,
                ext != null ? (String) ext.get("website") : null,
                ext != null ? (String) ext.get("location") : null,
                ext != null ? (String) ext.get("github") : null,
                ext != null ? (String) ext.get("weibo") : null,
                ext != null ? (String) ext.get("wechat") : null,
                ext != null ? (String) ext.get("qq") : null
        );
    }
}
