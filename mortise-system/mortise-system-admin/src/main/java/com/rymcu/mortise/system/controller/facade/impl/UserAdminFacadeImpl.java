package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.assembler.RoleAdminAssembler;
import com.rymcu.mortise.system.controller.assembler.UserAdminAssembler;
import com.rymcu.mortise.system.controller.facade.UserAdminFacade;
import com.rymcu.mortise.system.controller.request.UserStatusRequest;
import com.rymcu.mortise.system.controller.request.UserUpsertRequest;
import com.rymcu.mortise.system.controller.vo.PasswordResetVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserSearch;
import com.rymcu.mortise.system.query.UserQueryService;
import com.rymcu.mortise.system.service.command.UserCommandService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAdminFacadeImpl implements UserAdminFacade {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UserAdminFacadeImpl(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @Override
    public PageResult<UserVO> listUsers(UserSearch search) {
        PageResult<UserInfo> result = userQueryService.findUsers(PageQuery.of(search.getPageNum(), search.getPageSize()), search);
        return result.map(UserAdminAssembler::toUserVO);
    }

    @Override
    public UserVO getUserById(Long userId) {
        return UserAdminAssembler.toUserVO(userQueryService.findUserInfoById(userId));
    }

    @Override
    public Long createUser(UserUpsertRequest request) {
        return userCommandService.createUser(UserAdminAssembler.toUserInfo(request));
    }

    @Override
    public Boolean updateUser(Long userId, UserUpsertRequest request) {
        UserInfo userInfo = UserAdminAssembler.toUserInfo(request);
        userInfo.setId(userId);
        return userCommandService.updateUser(userInfo);
    }

    @Override
    public Boolean updateUserStatus(Long userId, UserStatusRequest request) {
        return userCommandService.updateStatus(userId, request.getStatus());
    }

    @Override
    public PasswordResetVO resetUserPassword(Long userId) {
        return new PasswordResetVO(userCommandService.resetPassword(userId));
    }

    @Override
    public Boolean bindUserRoles(Long userId, BindUserRoleInfo bindUserRoleInfo) {
        bindUserRoleInfo.setIdUser(userId);
        return userCommandService.bindUserRole(bindUserRoleInfo);
    }

    @Override
    public Boolean deleteUser(Long userId) {
        return userCommandService.deleteUser(userId);
    }

    @Override
    public Boolean batchDeleteUsers(BatchUpdateInfo batchUpdateInfo) {
        return userCommandService.batchDeleteUsers(batchUpdateInfo.getIds());
    }

    @Override
    public List<RoleVO> getUserRoles(Long userId) {
        return RoleAdminAssembler.toRoleVOs(userQueryService.findRolesByIdUser(userId));
    }

    @Override
    public Boolean bindRoleUsers(Long userId, BindUserRoleInfo bindUserRoleInfo) {
        bindUserRoleInfo.setIdUser(userId);
        return userCommandService.bindRoleUser(bindUserRoleInfo);
    }
}
