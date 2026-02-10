package com.rymcu.mortise.member.api.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.service.MemberService;

/**
 * 客户端会员服务
 * <p>
 * 扩展基础会员服务，添加客户端特定的业务方法
 *
 * @author ronger
 */
public interface ApiMemberService extends MemberService {

    /**
     * 根据用户名查询会员（用于登录）
     *
     * @param username 用户名
     * @return 会员信息
     */
    Member findByUsername(String username);

    /**
     * 根据邮箱查询会员（用于登录）
     *
     * @param email 邮箱
     * @return 会员信息
     */
    Member findByEmail(String email);

    /**
     * 根据手机号查询会员（用于登录）
     *
     * @param phone 手机号
     * @return 会员信息
     */
    Member findByPhone(String phone);

    /**
     * 注册会员
     *
     * @param member   会员信息
     * @param password 密码
     * @return 会员ID
     */
    Long register(Member member, String password);

    /**
     * 会员登录（用户名/邮箱/手机号）
     *
     * @param account  账号（用户名/邮箱/手机号）
     * @param password 密码
     * @return 会员信息
     */
    Member login(String account, String password);

    /**
     * 手机号验证码登录
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 会员信息
     */
    Member loginByPhone(String phone, String code);

    /**
     * 更新会员信息
     *
     * @param member 会员信息
     * @return 是否成功
     */
    Boolean updateMember(Member member);

    /**
     * 更新密码
     *
     * @param memberId    会员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    Boolean updatePassword(Long memberId, String oldPassword, String newPassword);

    /**
     * 重置密码
     *
     * @param account          账号
     * @param newPassword      新密码
     * @param verificationCode 验证码
     * @return 是否成功
     */
    Boolean resetPassword(String account, String newPassword, String verificationCode);

    /**
     * 验证邮箱
     *
     * @param memberId 会员ID
     * @param code     验证码
     * @return 是否成功
     */
    Boolean verifyEmail(Long memberId, String code);

    /**
     * 验证手机号
     *
     * @param memberId 会员ID
     * @param code     验证码
     * @return 是否成功
     */
    Boolean verifyPhone(Long memberId, String code);

    /**
     * 更新最后登录时间
     *
     * @param memberId 会员ID
     */
    void updateLastLoginTime(Long memberId);

    /**
     * 分页查询会员列表（管理端，未来应移至 admin 模块）
     *
     * @param page        分页对象
     * @param status      状态
     * @param memberLevel 会员等级
     * @param keyword     关键词
     * @return 分页数据
     * @deprecated 此方法应属于管理端功能，建议在 mortise-member-admin 模块中实现
     */
    @Deprecated
    Page<Member> findMemberList(Page<Member> page, Integer status, String memberLevel, String keyword);

    /**
     * 从 OAuth2 用户信息创建会员
     * <p>
     * 用于 OAuth2 登录时创建新会员，根据第三方账号信息生成用户名和其他默认信息
     *
     * @param member 包含昵称、头像等基本信息的会员对象
     * @return 新创建的会员ID
     */
    Long createMemberFromOAuth2(Member member);

    /**
     * 根据会员ID获取会员信息
     *
     * @param memberId 会员ID
     * @return 会员信息
     */
    Member getMemberById(Long memberId);

    /**
     * 修改用户名
     *
     * @param memberId    会员ID
     * @param newUsername 新用户名
     * @return 是否成功
     */
    Boolean updateUsername(Long memberId, String newUsername);
}
