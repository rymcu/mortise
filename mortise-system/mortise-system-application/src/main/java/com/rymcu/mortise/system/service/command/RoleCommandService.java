package com.rymcu.mortise.system.service.command;

import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;

import java.util.List;

public interface RoleCommandService {

    Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo);

    Boolean updateStatus(Long roleId, Integer status);

    Boolean deleteRole(Long roleId);

    Boolean batchDeleteRoles(List<Long> roleIds);

    Boolean bindRoleUser(BindRoleUserInfo bindRoleUserInfo);

    Long createRole(Role role);

    Boolean updateRole(Role role);
}
