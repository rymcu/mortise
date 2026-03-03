package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.UserSearch;

import java.util.List;
import java.util.Set;

/**
 * Created on 2024/4/13 21:25.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface UserService extends IService<User> {

    /**
     * @param account 用户账号
     * @return 更新成功数量
     */
    boolean updateLastOnlineTimeByAccount(String account);

    /**
     * 查询用户菜单权限
     *
     * @param idUser 用户 ID
     * @return 菜单权限
     */
    Set<String> findUserPermissionsByIdUser(Long idUser);

    /**
     * 获取用户角色
     *
     * @param idUser 用户 ID
     * @return 角色
     */
    Set<String> findUserRoleListByIdUser(Long idUser);

    /**
     * 查询用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    User findByAccount(String account);

    /**
     * 查询用户
     *
     * @param search 查询条件
     * @return 用户信息列表
     */
    Page<UserInfo> findUsers(Page<UserInfo> page, UserSearch search);

    UserInfo findUserInfoById(Long idUser);

    Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo);

    Boolean updateStatus(Long idUser, Integer status);

    String resetPassword(Long idUser);

    Boolean deleteUser(Long idUser);

    Boolean batchDeleteUsers(List<Long> idUserList);

    Boolean updateUserProfileInfo(UserProfileInfo userProfileInfo, User user);

    void updateLastLoginTimeByAccount(String account);

    String checkNickname(String nickname);

    String nextAccount();

    List<Role> findRolesByIdUser(Long idUser);

    Boolean bindRoleUser(BindUserRoleInfo bindUserRoleInfo);

    Long createUser(UserInfo userInfo);

    Boolean updateUser(UserInfo userInfo);

    /**
     * 发送邮箱更换验证码
     * <p>校验新邮箱未被注册，向新邮箱发送 6 位验证码，有效期 10 分钟</p>
     *
     * @param userId   当前用户 ID
     * @param newEmail 目标新邮箱
     */
    void sendEmailUpdateCode(Long userId, String newEmail);

    /**
     * 确认邮箱更换
     * <p>验证码匹配后将新邮箱写入数据库并清除验证码缓存</p>
     *
     * @param userId   当前用户 ID
     * @param newEmail 目标新邮箱
     * @param code     用户填写的验证码
     * @return 更新成功返回 true
     */
    Boolean confirmEmailUpdate(Long userId, String newEmail, String code);
}
