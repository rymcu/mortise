package com.rymcu.mortise.member.admin.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;
import com.rymcu.mortise.member.service.MemberService;

/**
 * 管理端会员服务扩展
 * <p>
 * 提供管理端需要的会员管理功能
 *
 * @author ronger
 */
public interface AdminMemberService extends MemberService {

    /**
     * 分页查询会员列表
     *
     * @param page   分页参数
     * @param search 查询条件
     * @return 会员信息列表
     */
    Page<MemberInfo> findMemberList(Page<MemberInfo> page, MemberSearch search);

    /**
     * 根据ID查询会员信息
     *
     * @param id 会员ID
     * @return 会员信息
     */
    MemberInfo findMemberInfoById(Long id);

    /**
     * 启用会员
     *
     * @param id 会员ID
     * @return 操作结果
     */
    Boolean enableMember(Long id);

    /**
     * 禁用会员
     *
     * @param id 会员ID
     * @return 操作结果
     */
    Boolean disableMember(Long id);

    /**
     * 更新会员状态
     *
     * @param id     会员ID
     * @param status 状态：0-正常, 1-禁用
     * @return 操作结果
     */
    Boolean updateStatus(Long id, Integer status);

}
