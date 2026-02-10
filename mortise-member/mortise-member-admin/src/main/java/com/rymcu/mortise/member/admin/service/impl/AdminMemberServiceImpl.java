package com.rymcu.mortise.member.admin.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;
import com.rymcu.mortise.member.admin.service.AdminMemberService;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.service.impl.MemberServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.rymcu.mortise.member.entity.table.MemberTableDef.MEMBER;

/**
 * 管理端会员服务实现
 *
 * @author ronger
 */
@Service
public class AdminMemberServiceImpl extends MemberServiceImpl implements AdminMemberService {

    @Override
    public Page<MemberInfo> findMemberList(Page<MemberInfo> page, MemberSearch search) {
        QueryWrapper queryWrapper = buildQueryWrapper(search);
        Page<Member> memberPage = new Page<>(page.getPageNumber(), page.getPageSize());
        memberPage = this.page(memberPage, queryWrapper);

        // 转换为 MemberInfo
        Page<MemberInfo> resultPage = new Page<>(page.getPageNumber(), page.getPageSize());
        resultPage.setTotalRow(memberPage.getTotalRow());
        resultPage.setRecords(memberPage.getRecords().stream()
                .map(this::convertToMemberInfo)
                .toList());
        return resultPage;
    }

    @Override
    public MemberInfo findMemberInfoById(Long id) {
        Member member = this.getById(id);
        if (member == null) {
            return null;
        }
        return convertToMemberInfo(member);
    }

    @Override
    public Boolean enableMember(Long id) {
        return updateStatus(id, 0);
    }

    @Override
    public Boolean disableMember(Long id) {
        return updateStatus(id, 1);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        Member member = new Member();
        member.setId(id);
        member.setStatus(status);
        return this.updateById(member);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper buildQueryWrapper(MemberSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(MEMBER.CREATED_TIME.desc());

        if (StringUtils.isNotBlank(search.getUsername())) {
            queryWrapper.and(MEMBER.USERNAME.like(search.getUsername()));
        }
        if (StringUtils.isNotBlank(search.getEmail())) {
            queryWrapper.and(MEMBER.EMAIL.like(search.getEmail()));
        }
        if (StringUtils.isNotBlank(search.getPhone())) {
            queryWrapper.and(MEMBER.PHONE.like(search.getPhone()));
        }
        if (StringUtils.isNotBlank(search.getNickname())) {
            queryWrapper.and(MEMBER.NICKNAME.like(search.getNickname()));
        }
        if (StringUtils.isNotBlank(search.getMemberLevel())) {
            queryWrapper.and(MEMBER.MEMBER_LEVEL.eq(search.getMemberLevel()));
        }
        if (Objects.nonNull(search.getStatus())) {
            queryWrapper.and(MEMBER.STATUS.eq(search.getStatus()));
        }
        if (StringUtils.isNotBlank(search.getQuery())) {
            queryWrapper.and(MEMBER.USERNAME.like(search.getQuery())
                    .or(MEMBER.NICKNAME.like(search.getQuery()))
                    .or(MEMBER.EMAIL.like(search.getQuery()))
                    .or(MEMBER.PHONE.like(search.getQuery())));
        }
        return queryWrapper;
    }

    /**
     * 实体转换为 VO
     */
    private MemberInfo convertToMemberInfo(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getPhone(),
                member.getName(),
                member.getNickname(),
                member.getAvatarUrl(),
                member.getGender(),
                member.getBirthDate(),
                member.getStatus(),
                member.getMemberLevel(),
                member.getPoints(),
                member.getBalance(),
                member.getRegisterSource(),
                member.getLastLoginTime(),
                member.getCreatedTime(),
                member.getUpdatedTime()
        );
    }

}
