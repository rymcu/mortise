package com.rymcu.mortise.member.admin.facade;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;

public interface AdminMemberFacade {

    Page<MemberInfo> listMembers(Page<MemberInfo> page, MemberSearch search);

    MemberInfo getMemberById(Long id);

    Boolean updateMemberStatus(Long id, Integer status);

    Boolean enableMember(Long id);

    Boolean disableMember(Long id);
}
