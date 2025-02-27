package com.rymcu.mortise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.core.exception.AccountExistsException;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.BindUserRoleInfo;
import com.rymcu.mortise.model.TokenUser;
import com.rymcu.mortise.model.UserInfo;
import com.rymcu.mortise.model.UserSearch;
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
public interface UserService {

    /**
     * @param account 用户账号
     * @return 更新成功数量
     */
    int updateLastOnlineTimeByAccount(String account);

    /**
     * 注册接口
     *
     * @param email    邮箱
     * @param nickname 昵称
     * @param password 密码
     * @param code     验证码
     * @return Boolean 注册成功标志
     */
    Boolean register(String email, String nickname, String password, String code) throws AccountExistsException;

    /**
     * 登录接口
     *
     * @param account  邮箱
     * @param password 密码
     * @return TokenUser
     */
    TokenUser login(String account, String password);

    /**
     * 刷新 token 接口
     *
     * @param refreshToken 刷新 token
     * @return TokenUser
     */
    TokenUser refreshToken(String refreshToken);

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
    List<UserInfo> findUsers(Page<UserInfo> page, UserSearch search);

    Boolean forgetPassword(String code, String password);

    UserInfo findUserInfoById(Long idUser);

    Boolean saveUser(UserInfo userInfo);

    Boolean updateUserInfo(UserInfo userInfo);

    Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo);

    Boolean updateStatus(Long idUser, Integer status);

    String resetPassword(Long idUser);

    Boolean updateDelFlag(Long idUser, Integer delFlag);

    TokenUser oauth2Login(OidcUser oidcUser, String registrationId);
}
