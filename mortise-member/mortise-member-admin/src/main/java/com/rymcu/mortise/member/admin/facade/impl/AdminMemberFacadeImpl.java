package com.rymcu.mortise.member.admin.facade.impl;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.member.admin.facade.AdminMemberFacade;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;
import com.rymcu.mortise.member.admin.service.AdminMemberService;
import org.springframework.stereotype.Component;

@Component
public class AdminMemberFacadeImpl implements AdminMemberFacade {

    private final AdminMemberService adminMemberService;

    public AdminMemberFacadeImpl(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @Override
    public PageResult<MemberInfo> listMembers(MemberSearch search) {
        Page<MemberInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = adminMemberService.findMemberList(page, search);
        return PageResult.of(
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow(),
                page.getRecords()
        );
    }

    @Override
    public MemberInfo getMemberById(Long id) {
        return adminMemberService.findMemberInfoById(id);
    }

    @Override
    public Boolean updateMemberStatus(Long id, Integer status) {
        return adminMemberService.updateStatus(id, status);
    }

    @Override
    public Boolean enableMember(Long id) {
        return adminMemberService.enableMember(id);
    }

    @Override
    public Boolean disableMember(Long id) {
        return adminMemberService.disableMember(id);
    }
}
