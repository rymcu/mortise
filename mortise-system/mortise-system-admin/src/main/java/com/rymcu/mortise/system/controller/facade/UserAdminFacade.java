package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.request.UserStatusRequest;
import com.rymcu.mortise.system.controller.request.UserUpsertRequest;
import com.rymcu.mortise.system.controller.vo.PasswordResetVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserSearch;

import java.util.List;

public interface UserAdminFacade {

    PageResult<UserVO> listUsers(UserSearch search);

    UserVO getUserById(Long userId);

    Long createUser(UserUpsertRequest request);

    Boolean updateUser(Long userId, UserUpsertRequest request);

    Boolean updateUserStatus(Long userId, UserStatusRequest request);

    PasswordResetVO resetUserPassword(Long userId);

    Boolean bindUserRoles(Long userId, BindUserRoleInfo bindUserRoleInfo);

    Boolean deleteUser(Long userId);

    Boolean batchDeleteUsers(BatchUpdateInfo batchUpdateInfo);

    List<RoleVO> getUserRoles(Long userId);

    Boolean bindRoleUsers(Long userId, BindUserRoleInfo bindUserRoleInfo);
}
