package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.model.UserProfile;
import com.rymcu.mortise.core.spi.UserProfileProvider;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.service.MemberService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
