package com.rymcu.mortise.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.model.BindRoleMenuInfo;
import com.rymcu.mortise.model.RoleSearch;

import java.util.List;
import java.util.Set;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface RoleService {
    List<Role> findRolesByIdUser(Long idUser);

    Boolean saveRole(Role role);

    Page<Role> findRoles(Page<Role> page, RoleSearch search);

    Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo);

    Boolean updateStatus(Long idRole, Integer status);

    Set<Long> findRoleMenus(Long idRole);

    Boolean deleteRole(Long idRole);

    Role findById(Long idRole);

    Boolean batchDeleteRoles(List<Long> idRoleList);
}
