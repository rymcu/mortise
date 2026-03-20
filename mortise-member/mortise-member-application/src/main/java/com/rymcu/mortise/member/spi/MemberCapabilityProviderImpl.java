package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.model.MemberCapability;
import com.rymcu.mortise.core.spi.MemberCapabilityProvider;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 基于 member 数据的会员能力提供者
 */
@Component
public class MemberCapabilityProviderImpl implements MemberCapabilityProvider {

    private final MemberService memberService;

    public MemberCapabilityProviderImpl(@Qualifier("memberServiceImpl") MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public Optional<MemberCapability> getMemberCapability(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        var member = memberService.getOne(QueryWrapper.create()
                .select(MEMBER.ID, MEMBER.STATUS, MEMBER.MEMBER_LEVEL)
                .where(MEMBER.ID.eq(userId)));
        if (member == null) {
            return Optional.empty();
        }
        return Optional.of(toCapability(member));
    }

    private MemberCapability toCapability(Member member) {
        var active = Integer.valueOf(0).equals(member.getStatus())
                && StringUtils.isNotBlank(member.getMemberLevel());
        return new MemberCapability(member.getId(), active, member.getMemberLevel());
    }
}