package com.rymcu.mortise.member.spi;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.model.MemberContact;
import com.rymcu.mortise.core.spi.MemberContactProvider;
import com.rymcu.mortise.member.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 基于 member 数据的联系信息提供者
 */
@Component
public class MemberContactProviderImpl implements MemberContactProvider {

    private final MemberService memberService;

    public MemberContactProviderImpl(@Qualifier("memberServiceImpl") MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public Optional<MemberContact> getMemberContact(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        var member = memberService.getOne(QueryWrapper.create()
                .select(MEMBER.ID, MEMBER.EMAIL, MEMBER.EMAIL_VERIFIED_TIME, MEMBER.STATUS)
                .where(MEMBER.ID.eq(userId)));
        if (member == null || StringUtils.isBlank(member.getEmail())) {
            return Optional.empty();
        }
        var emailVerified = Integer.valueOf(0).equals(member.getStatus())
                && member.getEmailVerifiedTime() != null;
        return Optional.of(new MemberContact(member.getId(), member.getEmail(), emailVerified));
    }
}