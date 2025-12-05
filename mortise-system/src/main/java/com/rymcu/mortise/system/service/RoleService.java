package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface RoleService extends IService<Role> {
    List<Role> findRolesByIdUser(Long idUser);

    Page<Role> findRoles(Page<Role> page, RoleSearch search);

    Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo);

    Boolean updateStatus(Long idRole, Integer status);

    List<Menu> findMenusByIdRole(Long idRole);

    Boolean deleteRole(Long idRole);

    Boolean batchDeleteRoles(List<Long> idRoleList);

    /**
     * 查找默认角色（用于新用户注册）
     * @return 默认角色
     */
    List<Role> findDefaultRole();

    Boolean bindRoleUser(@Valid BindRoleUserInfo bindRoleUserInfo);

    List<User> findUsersByIdRole(Long idRole);

    Long createRole(@Valid Role role);

    Boolean updateRole(@Valid Role role);
}
