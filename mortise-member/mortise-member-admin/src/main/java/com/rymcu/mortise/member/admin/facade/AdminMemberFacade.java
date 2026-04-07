package com.rymcu.mortise.member.admin.facade;

import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;

public interface AdminMemberFacade {

    PageResult<MemberInfo> listMembers(MemberSearch search);

    MemberInfo getMemberById(Long id);

    Boolean updateMemberStatus(Long id, Integer status);

    Boolean enableMember(Long id);

    Boolean disableMember(Long id);
}
