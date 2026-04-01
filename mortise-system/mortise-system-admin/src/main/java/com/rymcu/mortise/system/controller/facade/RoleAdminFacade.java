package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.request.RoleStatusRequest;
import com.rymcu.mortise.system.controller.request.RoleUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;

import java.util.List;

public interface RoleAdminFacade {

    PageResult<RoleVO> listRoles(RoleSearch search);

    RoleVO getRoleById(Long roleId);

    Long createRole(RoleUpsertRequest request);

    Boolean updateRole(Long roleId, RoleUpsertRequest request);

    Boolean updateRoleStatus(Long roleId, RoleStatusRequest request);

    List<UserVO> getRoleUsers(Long roleId);

    Boolean bindRoleUsers(Long roleId, BindRoleUserInfo bindRoleUserInfo);

    List<MenuVO> getRoleMenus(Long roleId);

    Boolean bindRoleMenus(Long roleId, BindRoleMenuInfo bindRoleMenuInfo);

    Boolean deleteRole(Long roleId);

    Boolean batchDeleteRoles(BatchUpdateInfo batchUpdateInfo);
}
