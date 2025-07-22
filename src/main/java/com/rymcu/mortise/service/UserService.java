package com.rymcu.mortise.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.core.exception.AccountExistsException;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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

    Boolean saveUser(UserInfo userInfo);

    Boolean updateUserInfo(UserInfo userInfo);

    Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo);

    Boolean updateStatus(Long idUser, Integer status);

    String resetPassword(Long idUser);

    Boolean updateDelFlag(Long idUser, Integer delFlag);

    Boolean updateUserProfileInfo(UserProfileInfo userProfileInfo, User user);

    void updateLastLoginTimeByAccount(String account);

    String checkNickname(String nickname);

    String nextAccount();
}
